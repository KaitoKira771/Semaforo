package semaforo;

public enum EstadoSemaforo {
    A_VERDE("Via A verde / Via B vermelha", "1000010"),
    A_AMARELO("Via A amarela / Via B vermelha", "0100010"),
    B_VERDE("Via A vermelha / Via B verde", "0011000"),
    B_AMARELO("Via A vermelha / Via B amarela", "0010100"),
    PEDESTRE("Pedestre liberado / Carros parados", "0010011"),
    EMERGENCIA("Modo emergência / Todas as vias em vermelho", "0010010");

    private final String descricao;
    private final String binario;

    EstadoSemaforo(String descricao, String binario) {
        this.descricao = descricao;
        this.binario = binario;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getBinario() {
        return binario;
    }
}