"""
recommendation/engine.py
========================
TF-IDF + Cosine Similarity recommendation engine.

How it works:
1. Fetch all experts from the DB.
2. Create a "feature text" per expert from expertise + skills + categories + bio.
3. Vectorize with TF-IDF.
4. Compute cosine similarity matrix.
5. For a given user:
   - Warm users: use their booked/favorited experts as seed → find similar experts.
   - Cold-start: return top-rated + most-booked + trending experts.
"""

import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from typing import List, Dict, Any, Optional
import logging

logger = logging.getLogger(__name__)


class RecommendationEngine:
    """
    Content-based filtering recommendation engine using TF-IDF.
    Must be rebuilt when expert data changes significantly.
    """

    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            stop_words="english",
            ngram_range=(1, 2),      # Unigrams + bigrams
            max_features=5000,
            sublinear_tf=True,       # Apply log normalization to TF
        )
        self._df: Optional[pd.DataFrame] = None
        self._tfidf_matrix = None
        self._similarity_matrix = None
        self._is_fitted = False

    def _build_feature_text(self, expert: Dict[str, Any]) -> str:
        """
        Concatenate expert fields into a single feature text for TF-IDF.
        Repeats high-signal fields to boost their weight.
        """
        parts = [
            (expert.get("expertise") or "") * 3,    # Triple weight
            (expert.get("skills") or "") * 2,        # Double weight
            expert.get("categories") or "",
            expert.get("bio") or "",
            expert.get("languages") or "",
        ]
        return " ".join(filter(None, parts)).lower()

    def fit(self, experts: List[Dict[str, Any]]) -> None:
        """
        Build the TF-IDF matrix and cosine similarity matrix from expert data.

        Args:
            experts: List of expert dicts with keys:
                     id, expertise, skills, categories, bio, languages,
                     average_rating, total_bookings.
        """
        if not experts:
            logger.warning("No experts provided to recommendation engine.")
            self._is_fitted = False
            return

        self._df = pd.DataFrame(experts)
        self._df["feature_text"] = self._df.apply(
            lambda row: self._build_feature_text(row.to_dict()), axis=1
        )

        # Fit TF-IDF
        self._tfidf_matrix = self.vectorizer.fit_transform(self._df["feature_text"])

        # Cosine similarity matrix (n_experts × n_experts)
        self._similarity_matrix = cosine_similarity(self._tfidf_matrix, self._tfidf_matrix)

        self._is_fitted = True
        logger.info(
            f"Recommendation engine fitted with {len(experts)} experts. "
            f"Matrix shape: {self._similarity_matrix.shape}"
        )

    def get_similar_experts(
        self,
        seed_expert_ids: List[str],
        exclude_ids: List[str],
        top_n: int = 10,
        min_score: float = 0.05,
    ) -> List[Dict[str, Any]]:
        """
        Return top-N experts most similar to the seed experts.

        Args:
            seed_expert_ids: Expert IDs the user has interacted with.
            exclude_ids:     Expert IDs to exclude (already seen/interacted with).
            top_n:           Number of recommendations to return.
            min_score:       Minimum similarity threshold.

        Returns:
            List of dicts with expert info + similarity_score.
        """
        if not self._is_fitted or self._df is None:
            return []

        id_to_idx = {str(eid): idx for idx, eid in enumerate(self._df["id"])}
        seed_indices = [id_to_idx[eid] for eid in seed_expert_ids if eid in id_to_idx]

        if not seed_indices:
            return []

        # Average similarity scores across all seed experts
        avg_scores = np.mean(self._similarity_matrix[seed_indices], axis=0)

        # Build scored list excluding seeds and already excluded experts
        exclude_set = set(exclude_ids) | set(seed_expert_ids)
        scored = []
        for idx, score in enumerate(avg_scores):
            expert_id = str(self._df.iloc[idx]["id"])
            if expert_id not in exclude_set and score >= min_score:
                expert_data = self._df.iloc[idx].to_dict()
                expert_data["similarity_score"] = float(score)
                scored.append(expert_data)

        # Sort by similarity score descending
        scored.sort(key=lambda x: x["similarity_score"], reverse=True)
        return scored[:top_n]

    def get_cold_start_recommendations(
        self,
        top_n: int = 10,
        exclude_ids: Optional[List[str]] = None,
    ) -> List[Dict[str, Any]]:
        """
        Cold-start strategy: blend top-rated + most-booked experts.
        Used for new users with no booking/favorite history.

        Returns:
            List of expert dicts with a 'recommendation_score'.
        """
        if not self._is_fitted or self._df is None:
            return []

        df = self._df.copy()
        exclude_set = set(exclude_ids or [])
        df = df[~df["id"].astype(str).isin(exclude_set)]

        if df.empty:
            return []

        # Normalize rating (0–1) and bookings (0–1)
        max_rating = df["average_rating"].max() or 1
        max_bookings = df["total_bookings"].max() or 1

        df["norm_rating"] = df["average_rating"] / max_rating
        df["norm_bookings"] = df["total_bookings"] / max_bookings

        # Weighted blend: 60% rating + 40% popularity
        df["recommendation_score"] = (
            df["norm_rating"] * 0.6 + df["norm_bookings"] * 0.4
        )

        top = df.nlargest(top_n, "recommendation_score")
        return top.to_dict(orient="records")

    @property
    def is_fitted(self) -> bool:
        return self._is_fitted


# Singleton instance — shared across the application
recommendation_engine = RecommendationEngine()
