package biz.gelicon.core.artifacts;

/**
 * Язык ресурса-артефакта
 */
public enum ArtifactTranKinds {
    /*
     * 1	01	KAV/X
     * 2	02	GOAL
     * 3	03	Java
     */
    KAV_X,
    GOAL,
    JAVA;

    public int getResourceTranType() {
        return this.ordinal() + 1;
    }

    public static int[] getExternalResourceTranType() {
        return new int[]{
                KAV_X.getResourceTranType(),
                GOAL.getResourceTranType(),
                JAVA.getResourceTranType()
        };
    }

}
