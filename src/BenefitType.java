public enum BenefitType {
    CH31("CH31 - VR&E"),
    CH33("CH33 - Post-9/11 GI Bill"),
    CH33D("CH33D - Transferred Post-9/11"),
    CH35("CH35 - Dependents Education Assistance");

    private final String displayName;

    BenefitType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}