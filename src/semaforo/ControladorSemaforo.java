package semaforo;

import java.util.ArrayList;
import java.util.List;

public class ControladorSemaforo {

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

    public EstadoSemaforo decidirDestino(EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        List<EstadoSemaforo> sequencia = montarSequencia(atual, viaA, viaB, pedestre, emergencia);
        if (sequencia.isEmpty()) {
            return atual;
        }
        return sequencia.get(sequencia.size() - 1);
    }

    public List<EstadoSemaforo> montarSequencia(EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        List<EstadoSemaforo> sequencia = new ArrayList<EstadoSemaforo>();

        if (emergencia) {
            sequencia.add(EstadoSemaforo.EMERGENCIA);
            return sequencia;
        }

        if (modoOperacao == ModoOperacao.VIAS_PARALELAS) {
            montarSequenciaParalela(sequencia, atual, pedestre);
        } else {
            montarSequenciaCruzamento(sequencia, atual, viaA, viaB, pedestre);
        }

        return sequencia;
    }

    private void montarSequenciaParalela(List<EstadoSemaforo> sequencia, EstadoSemaforo atual, boolean pedestre) {
        if (atual == EstadoSemaforo.PEDESTRE) {
            sequencia.add(EstadoSemaforo.AB_VERDE);
            return;
        }

        if (atual != EstadoSemaforo.AB_VERDE && atual != EstadoSemaforo.AB_AMARELO) {
            if (pedestre) {
                sequencia.add(EstadoSemaforo.AB_VERDE);
                sequencia.add(EstadoSemaforo.AB_AMARELO);
                sequencia.add(EstadoSemaforo.PEDESTRE);
                sequencia.add(EstadoSemaforo.AB_VERDE);
            } else {
                sequencia.add(EstadoSemaforo.AB_VERDE);
            }
            return;
        }

        if (pedestre) {
            if (atual == EstadoSemaforo.AB_VERDE) {
                sequencia.add(EstadoSemaforo.AB_AMARELO);
            }
            sequencia.add(EstadoSemaforo.PEDESTRE);
            sequencia.add(EstadoSemaforo.AB_VERDE);
        } else {
            sequencia.add(EstadoSemaforo.AB_VERDE);
        }
    }

    private void montarSequenciaCruzamento(List<EstadoSemaforo> sequencia, EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre) {
        EstadoSemaforo estadoBase = normalizarEstadoCruzamento(atual, viaA, viaB);

        if (estadoBase == EstadoSemaforo.A_VERDE) {
            montarPartindoDeA(sequencia, viaA, viaB, pedestre);
        } else {
            montarPartindoDeB(sequencia, viaA, viaB, pedestre);
        }
    }

    private EstadoSemaforo normalizarEstadoCruzamento(EstadoSemaforo atual, boolean viaA, boolean viaB) {
        if (atual == EstadoSemaforo.A_VERDE || atual == EstadoSemaforo.A_AMARELO) {
            return EstadoSemaforo.A_VERDE;
        }

        if (atual == EstadoSemaforo.B_VERDE || atual == EstadoSemaforo.B_AMARELO) {
            return EstadoSemaforo.B_VERDE;
        }

        return escolherViaAposPedestreOuInicio(viaA, viaB);
    }

    private EstadoSemaforo escolherViaAposPedestreOuInicio(boolean viaA, boolean viaB) {
        if (viaA && !viaB) {
            return EstadoSemaforo.A_VERDE;
        }

        if (viaB && !viaA) {
            return EstadoSemaforo.B_VERDE;
        }

        if (viaA && viaB) {
            if (modoPrioridade == ModoPrioridade.VIA_A) {
                return EstadoSemaforo.A_VERDE;
            }

            if (modoPrioridade == ModoPrioridade.VIA_B) {
                return EstadoSemaforo.B_VERDE;
            }

            ultimaPreferenciaFoiA = !ultimaPreferenciaFoiA;
            if (ultimaPreferenciaFoiA) {
                return EstadoSemaforo.A_VERDE;
            } else {
                return EstadoSemaforo.B_VERDE;
            }
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

        if (viaB && !viaA) {
            return EstadoSemaforo.B_VERDE;
        }

        if (viaA && viaB) {
            return preferencial;
        }

        return preferencial;
    }

    private void montarPartindoDeA(List<EstadoSemaforo> sequencia, boolean viaA, boolean viaB, boolean pedestre) {
        if (!viaB && !pedestre) {
            sequencia.add(EstadoSemaforo.A_VERDE);
            return;
        }

        sequencia.add(EstadoSemaforo.A_AMARELO);

        if (viaB) {
            sequencia.add(EstadoSemaforo.B_VERDE);

            if (pedestre) {
                sequencia.add(EstadoSemaforo.B_AMARELO);
                sequencia.add(EstadoSemaforo.PEDESTRE);
                sequencia.add(escolherRetornoAposPedestre(viaA, viaB, EstadoSemaforo.A_VERDE));
            }
        } else if (pedestre) {
            sequencia.add(EstadoSemaforo.PEDESTRE);
            sequencia.add(escolherRetornoAposPedestre(viaA, viaB, EstadoSemaforo.A_VERDE));
        }
    }

    private void montarPartindoDeB(List<EstadoSemaforo> sequencia, boolean viaA, boolean viaB, boolean pedestre) {
        if (!viaA && !pedestre) {
            sequencia.add(EstadoSemaforo.B_VERDE);
            return;
        }

        sequencia.add(EstadoSemaforo.B_AMARELO);

        if (viaA) {
            sequencia.add(EstadoSemaforo.A_VERDE);

            if (pedestre) {
                sequencia.add(EstadoSemaforo.A_AMARELO);
                sequencia.add(EstadoSemaforo.PEDESTRE);
                sequencia.add(escolherRetornoAposPedestre(viaA, viaB, EstadoSemaforo.B_VERDE));
            }
        } else if (pedestre) {
            sequencia.add(EstadoSemaforo.PEDESTRE);
            sequencia.add(escolherRetornoAposPedestre(viaA, viaB, EstadoSemaforo.B_VERDE));
        }
    }

    public int getDuracaoEstado(EstadoSemaforo estado) {
        switch (estado) {
            case A_VERDE:
            case B_VERDE:
                return 6;

            case AB_VERDE:
                return 8;

            case A_AMARELO:
            case B_AMARELO:
            case AB_AMARELO:
                return 2;

            case PEDESTRE:
                return 5;

            case EMERGENCIA:
                return 3;

            default:
                return 3;
        }
    }

    public String getDescricaoTempos() {
        if (modoOperacao == ModoOperacao.VIAS_PARALELAS) {
            return "Tempos: verde conjunto = 8s | amarelo conjunto = 2s | pedestre = 5s";
        }
        return "Tempos: verde = 6s | amarelo = 2s | pedestre = 5s";
    }

    public String explicarRegra(boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (emergencia) {
            return "Regra aplicada: modo emergência ativado, então todas as vias ficam em vermelho.";
        }

        if (modoOperacao == ModoOperacao.VIAS_PARALELAS) {
            if (pedestre) {
                return "Regra aplicada: no modo vias paralelas, as vias A e B ficam verdes juntas e depois intercalam com o pedestre.";
            }
            return "Regra aplicada: no modo vias paralelas, as vias A e B funcionam simultaneamente.";
        }

        if (viaA && viaB && pedestre) {
            return "Regra aplicada: no modo cruzamento, a Via A intercala com a Via B e com o pedestre.";
        }

        if (viaA && viaB) {
            return "Regra aplicada: no modo cruzamento, as vias A e B se alternam.";
        }

        if (pedestre) {
            return "Regra aplicada: no modo cruzamento, a fase de pedestre entra no ciclo.";
        }

        if (viaA) {
            return "Regra aplicada: demanda apenas na Via A.";
        }

        if (viaB) {
            return "Regra aplicada: demanda apenas na Via B.";
        }

        return "Regra aplicada: sem novas demandas, o sistema mantém o comportamento padrão do modo selecionado.";
    }
}