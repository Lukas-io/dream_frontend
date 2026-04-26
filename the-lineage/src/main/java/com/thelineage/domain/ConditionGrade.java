package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Curator-assigned condition grade. Replaces the seller's proposed grade on authentication.
          MINT       - Deadstock or as-new; original packaging where applicable.
          EXCELLENT  - Worn lightly or well-preserved; no defects.
          VERY_GOOD  - Worn but well-cared-for; minor signs of use.
          GOOD       - Visible wear; structurally sound.
          FAIR       - Heavy wear; collector-grade only.""")
public enum ConditionGrade {
    MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR
}
