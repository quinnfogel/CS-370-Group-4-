public class MonthlyAllowanceCalculator {

    public static double calculateMonthlyAllowance(BenefitType benefitType, String unitLoadCategory) {
        if (benefitType == null || unitLoadCategory == null || unitLoadCategory.isBlank()) {
            return 0.0;
        }

        return switch (benefitType) {
            case CH33, CH33D, CH31 -> calculateChapter33StyleRate(unitLoadCategory);
            case CH35 -> calculateChapter35Rate(unitLoadCategory);
        };
    }

    private static double calculateChapter33StyleRate(String unitLoadCategory) {
        return switch (unitLoadCategory) {
            case "FullTime" -> 3987.0;
            case "ThreeQuarterTime" -> 3190.0;
            case "HalfTime" -> 2392.0;
            case "LessThanHalfTime" -> 0.0;
            default -> 0.0;
        };
    }

    private static double calculateChapter35Rate(String unitLoadCategory) {
        return switch (unitLoadCategory) {
            case "FullTime" -> 1536.0;
            case "ThreeQuarterTime" -> 1214.0;
            case "HalfTime" -> 890.0;
            case "LessThanHalfTime" -> 0.0;
            default -> 0.0;
        };
    }

    public static String formatAllowance(double amount) {
        return "$" + String.format("%,.2f", amount) + " / month";
    }
}