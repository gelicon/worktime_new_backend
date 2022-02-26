package biz.gelicon.core.artifacts;

/**
 * Тип конфигурации для артефакта
 */
public enum ArtifactCapconfigKind {
    /*
     * 1	01	Базовая конфигурация
     * 2	02	Конфигурация пользователя
     */
    BASIC,
    CUSTOM;

    public int getResourceCapconfigType() {
        return this.ordinal() + 1;
    }

    public static int[] getExternalResourceTranType() {
        return new int[]{
                BASIC.getResourceCapconfigType(),
                CUSTOM.getResourceCapconfigType()
        };
    }

}
