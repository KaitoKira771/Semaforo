package semaforo;

import java.util.ArrayList;
import java.util.List;

public class ControladorSemaforo {

    /*
     * =========================================================
     * ÁREA DE EDIÇÃO RÁPIDA - TEMPOS DO SISTEMA
     * =========================================================
     */
    private static final int TEMPO_VERDE_CRUZAMENTO = 6;
    private static final int TEMPO_VERDE_PARALELAS = 8;
    private static final int TEMPO_AMARELO = 2;
    private static final int TEMPO_PEDESTRE = 10;
    private static final int TEMPO_EMERGENCIA = 6;

    public enum ModoPrioridade {
        ALTERNADA("Alternada"),
        VIA_A("Priorizar Via A"),
        VIA_B("Priorizar Via B");

        private final String descricao;

        ModoPrioridade(String descricao) {
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    public enum ModoOperacao {
        CRUZAMENTO("Cruzamento"),
        VIAS_PARALELAS("Vias paralelas");

        private final String descricao;

        ModoOperacao(String descricao) {
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    private boolean ultimaPreferenciaFoiA = false;
    private ModoPrioridade modoPrioridade = ModoPrioridade.ALTERNADA;
    private ModoOperacao modoOperacao = ModoOperacao.CRUZAMENTO;

    public void setModoPrioridade(ModoPrioridade modoPrioridade) {
        this.modoPrioridade = modoPrioridade;
    }

    public ModoPrioridade getModoPrioridade() {
        return modoPrioridade;
    }

    public void setModoOperacao(ModoOperacao modoOperacao) {
        this.modoOperacao = modoOperacao;
    }

    public ModoOperacao getModoOperacao() {
        return modoOperacao;
    }

    public void resetar() {
        ultimaPreferenciaFoiA = false;
        modoPrioridade = ModoPrioridade.ALTERNADA;
        modoOperacao = ModoOperacao.CRUZAMENTO;
    }

    /*
     * =========================================================
     * DEFINIÇÃO DE REGRAS LÓGICAS
     * =========================================================
     *
     * Variáveis proposicionais:
     * A = há veículo na via A
     * B = há veículo na via B
     * P = há pedestre aguardando
     * E = emergência ativada
     *
     * Operadores usados:
     * AND  = &&
     * OR   = ||
     * NOT  = !
     *
     * Regras principais:
     *
     * R1) E -> EMERGENCIA
     *     Se há emergência, o sistema entra em EMERGENCIA.
     *
     * R2) P && !E -> PEDESTRE
     *     Se há pedestre e não há emergência, o pedestre entra no ciclo.
     *
     * R3) A && !B && !E -> A_VERDE
     *     Se há veículo apenas na via A, a via A recebe verde.
     *
     * R4) !A && B && !E -> B_VERDE
     *     Se há veículo apenas na via B, a via B recebe verde.
     *
     * R5) A && B && !P && !E -> alternância ou prioridade
     *     Se há veículos nas duas vias, o sistema alterna ou usa a prioridade definida.
     *
     * R6) (A || B) && !P && !E no modo VIAS_PARALELAS -> AB_VERDE
     *     Em vias paralelas, as duas vias podem operar juntas.
     *
     * R7) Mudanças de fluxo passam por AMARELO antes do próximo estado.
     */

    private boolean regraEmergencia(boolean emergencia) {
        return emergencia;
    }

    private boolean regraPedestre(boolean pedestre, boolean emergencia) {
        return pedestre && !emergencia;
    }

    private boolean regraSomenteViaA(boolean viaA, boolean viaB, boolean emergencia) {
        return viaA && !viaB && !emergencia;
    }

    private boolean regraSomenteViaB(boolean viaA, boolean viaB, boolean emergencia) {
        return !viaA && viaB && !emergencia;
    }

    private boolean regraDuasVias(boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        return viaA && viaB && !pedestre && !emergencia;
    }

    private boolean regraParalelaComFluxo(boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        return (viaA || viaB) && !pedestre && !emergencia;
    }

    public EstadoSemaforo decidirDestino(EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        List<EstadoSemaforo> sequencia = montarSequencia(atual, viaA, viaB, pedestre, emergencia);
        if (sequencia.isEmpty()) {
            return atual;
        }
        return sequencia.get(sequencia.size() - 1);
    }

    public List<EstadoSemaforo> montarSequencia(EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        List<EstadoSemaforo> sequencia = new ArrayList<>();

        if (regraEmergencia(emergencia)) {
            sequencia.add(EstadoSemaforo.EMERGENCIA);
            return sequencia;
        }

        if (modoOperacao == ModoOperacao.VIAS_PARALELAS) {
            montarSequenciaParalela(sequencia, atual, viaA, viaB, pedestre, emergencia);
        } else {
            montarSequenciaCruzamento(sequencia, atual, viaA, viaB, pedestre, emergencia);
        }

        return sequencia;
    }

    private void montarSequenciaParalela(List<EstadoSemaforo> sequencia, EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (regraPedestre(pedestre, emergencia)) {
            if (atual == EstadoSemaforo.AB_VERDE) {
                sequencia.add(EstadoSemaforo.AB_AMARELO);
            } else if (atual != EstadoSemaforo.AB_AMARELO && atual != EstadoSemaforo.PEDESTRE) {
                sequencia.add(EstadoSemaforo.AB_VERDE);
                sequencia.add(EstadoSemaforo.AB_AMARELO);
            }

            sequencia.add(EstadoSemaforo.PEDESTRE);
            sequencia.add(EstadoSemaforo.AB_VERDE);
            return;
        }

        if (atual == EstadoSemaforo.PEDESTRE) {
            sequencia.add(EstadoSemaforo.AB_VERDE);
            return;
        }

        if (regraParalelaComFluxo(viaA, viaB, pedestre, emergencia)) {
            sequencia.add(EstadoSemaforo.AB_VERDE);
            return;
        }

        sequencia.add(EstadoSemaforo.AB_VERDE);
    }

    private void montarSequenciaCruzamento(List<EstadoSemaforo> sequencia, EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        EstadoSemaforo estadoBase = normalizarEstadoCruzamento(atual, viaA, viaB, pedestre, emergencia);

        if (estadoBase == EstadoSemaforo.A_VERDE) {
            montarPartindoDeA(sequencia, viaA, viaB, pedestre, emergencia);
        } else {
            montarPartindoDeB(sequencia, viaA, viaB, pedestre, emergencia);
        }
    }

    private EstadoSemaforo normalizarEstadoCruzamento(EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (atual == EstadoSemaforo.A_VERDE || atual == EstadoSemaforo.A_AMARELO) {
            return EstadoSemaforo.A_VERDE;
        }

        if (atual == EstadoSemaforo.B_VERDE || atual == EstadoSemaforo.B_AMARELO) {
            return EstadoSemaforo.B_VERDE;
        }

        return escolherViaAposPedestreOuInicio(viaA, viaB, pedestre, emergencia);
    }

    private EstadoSemaforo escolherViaAposPedestreOuInicio(boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (regraSomenteViaA(viaA, viaB, emergencia)) {
            return EstadoSemaforo.A_VERDE;
        }

        if (regraSomenteViaB(viaA, viaB, emergencia)) {
            return EstadoSemaforo.B_VERDE;
        }

        if (regraDuasVias(viaA, viaB, pedestre, emergencia)) {
            if (modoPrioridade == ModoPrioridade.VIA_A) {
                return EstadoSemaforo.A_VERDE;
            }

            if (modoPrioridade == ModoPrioridade.VIA_B) {
                return EstadoSemaforo.B_VERDE;
            }

            ultimaPreferenciaFoiA = !ultimaPreferenciaFoiA;
            return ultimaPreferenciaFoiA ? EstadoSemaforo.A_VERDE : EstadoSemaforo.B_VERDE;
        }

        if (modoPrioridade == ModoPrioridade.VIA_B) {
            return EstadoSemaforo.B_VERDE;
        }

        return EstadoSemaforo.A_VERDE;
    }

    private EstadoSemaforo escolherRetornoAposPedestre(boolean viaA, boolean viaB, EstadoSemaforo preferencial) {
        if (viaA && !viaB) {
            return EstadoSemaforo.A_VERDE;
        }

        if (!viaA && viaB) {
            return EstadoSemaforo.B_VERDE;
        }

        if (viaA && viaB) {
            return preferencial;
        }

        return preferencial;
    }

    private void montarPartindoDeA(List<EstadoSemaforo> sequencia, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (!viaB && !pedestre && !emergencia) {
            sequencia.add(EstadoSemaforo.A_VERDE);
            return;
        }

        sequencia.add(EstadoSemaforo.A_AMARELO);

        if (viaB) {
            sequencia.add(EstadoSemaforo.B_VERDE);

            if (regraPedestre(pedestre, emergencia)) {
                sequencia.add(EstadoSemaforo.B_AMARELO);
                sequencia.add(EstadoSemaforo.PEDESTRE);
                sequencia.add(escolherRetornoAposPedestre(viaA, viaB, EstadoSemaforo.A_VERDE));
            }
        } else if (regraPedestre(pedestre, emergencia)) {
            sequencia.add(EstadoSemaforo.PEDESTRE);
            sequencia.add(escolherRetornoAposPedestre(viaA, viaB, EstadoSemaforo.A_VERDE));
        }
    }

    private void montarPartindoDeB(List<EstadoSemaforo> sequencia, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (!viaA && !pedestre && !emergencia) {
            sequencia.add(EstadoSemaforo.B_VERDE);
            return;
        }

        sequencia.add(EstadoSemaforo.B_AMARELO);

        if (viaA) {
            sequencia.add(EstadoSemaforo.A_VERDE);

            if (regraPedestre(pedestre, emergencia)) {
                sequencia.add(EstadoSemaforo.A_AMARELO);
                sequencia.add(EstadoSemaforo.PEDESTRE);
                sequencia.add(escolherRetornoAposPedestre(viaA, viaB, EstadoSemaforo.B_VERDE));
            }
        } else if (regraPedestre(pedestre, emergencia)) {
            sequencia.add(EstadoSemaforo.PEDESTRE);
            sequencia.add(escolherRetornoAposPedestre(viaA, viaB, EstadoSemaforo.B_VERDE));
        }
    }

    public int getDuracaoEstado(EstadoSemaforo estado) {
        switch (estado) {
            case A_VERDE:
            case B_VERDE:
                return TEMPO_VERDE_CRUZAMENTO;

            case AB_VERDE:
                return TEMPO_VERDE_PARALELAS;

            case A_AMARELO:
            case B_AMARELO:
            case AB_AMARELO:
                return TEMPO_AMARELO;

            case PEDESTRE:
                return TEMPO_PEDESTRE;

            case EMERGENCIA:
                return TEMPO_EMERGENCIA;

            default:
                return 3;
        }
    }

    public String getDescricaoTempos() {
        if (modoOperacao == ModoOperacao.VIAS_PARALELAS) {
            return "Tempos: verde conjunto = " + TEMPO_VERDE_PARALELAS
                    + "s | amarelo = " + TEMPO_AMARELO
                    + "s | pedestre = " + TEMPO_PEDESTRE
                    + "s | emergência = " + TEMPO_EMERGENCIA + "s";
        }

        return "Tempos: verde = " + TEMPO_VERDE_CRUZAMENTO
                + "s | amarelo = " + TEMPO_AMARELO
                + "s | pedestre = " + TEMPO_PEDESTRE
                + "s | emergência = " + TEMPO_EMERGENCIA + "s";
    }

    public String explicarRegra(boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (regraEmergencia(emergencia)) {
            return "modo emergência ativado, então as luzes amarelas das vias piscam e o pedestre permanece fechado.";
        }

        if (modoOperacao == ModoOperacao.VIAS_PARALELAS) {
            if (regraPedestre(pedestre, emergencia)) {
                return "no modo vias paralelas, as vias A e B ficam verdes juntas e depois intercalam com o pedestre.";
            }
            return "no modo vias paralelas, as vias A e B funcionam simultaneamente.";
        }

        if (viaA && viaB && pedestre && !emergencia) {
            return "no modo cruzamento, a Via A intercala com a Via B e com o pedestre.";
        }

        if (regraDuasVias(viaA, viaB, pedestre, emergencia)) {
            return "no modo cruzamento, as vias A e B se alternam.";
        }

        if (regraPedestre(pedestre, emergencia)) {
            return "no modo cruzamento, a fase de pedestre entra no ciclo.";
        }

        if (regraSomenteViaA(viaA, viaB, emergencia)) {
            return "demanda apenas na Via A.";
        }

        if (regraSomenteViaB(viaA, viaB, emergencia)) {
            return "demanda apenas na Via B.";
        }

        return "sem novas demandas, o sistema mantém o comportamento padrão do modo selecionado.";
    }
}
