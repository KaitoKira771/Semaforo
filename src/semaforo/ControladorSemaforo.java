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

    private boolean ultimaPreferenciaFoiA = false;
    private ModoPrioridade modoPrioridade = ModoPrioridade.ALTERNADA;

    public void setModoPrioridade(ModoPrioridade modoPrioridade) {
        this.modoPrioridade = modoPrioridade;
    }

    public ModoPrioridade getModoPrioridade() {
        return modoPrioridade;
    }

    public void resetar() {
        ultimaPreferenciaFoiA = false;
        modoPrioridade = ModoPrioridade.ALTERNADA;
    }

    public EstadoSemaforo decidirDestino(boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (emergencia) {
            return EstadoSemaforo.EMERGENCIA;
        }

        if (pedestre) {
            return EstadoSemaforo.PEDESTRE;
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

        if (viaA) {
            return EstadoSemaforo.A_VERDE;
        }

        if (viaB) {
            return EstadoSemaforo.B_VERDE;
        }

        return EstadoSemaforo.A_VERDE;
    }

    public List<EstadoSemaforo> montarSequencia(EstadoSemaforo atual, boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        EstadoSemaforo destino = decidirDestino(viaA, viaB, pedestre, emergencia);
        List<EstadoSemaforo> sequencia = new ArrayList<EstadoSemaforo>();

        if (atual == destino) {
            sequencia.add(destino);
            return sequencia;
        }

        if (destino == EstadoSemaforo.EMERGENCIA) {
            sequencia.add(EstadoSemaforo.EMERGENCIA);
            return sequencia;
        }

        if (atual == EstadoSemaforo.A_VERDE && destino != EstadoSemaforo.A_VERDE) {
            sequencia.add(EstadoSemaforo.A_AMARELO);
        } else if (atual == EstadoSemaforo.B_VERDE && destino != EstadoSemaforo.B_VERDE) {
            sequencia.add(EstadoSemaforo.B_AMARELO);
        }

        sequencia.add(destino);
        return sequencia;
    }

    public int getDuracaoEstado(EstadoSemaforo estado) {
        switch (estado) {
            case A_VERDE:
            case B_VERDE:
                return 5;

            case A_AMARELO:
            case B_AMARELO:
                return 2;

            case PEDESTRE:
                return 4;

            case EMERGENCIA:
                return 3;

            default:
                return 3;
        }
    }

    public String explicarRegra(boolean viaA, boolean viaB, boolean pedestre, boolean emergencia) {
        if (emergencia) {
            return "Regra aplicada: modo emergência ativado, então todas as vias ficam em vermelho.";
        }

        if (pedestre) {
            return "Regra aplicada: há pedestre aguardando, então o sistema prioriza a travessia.";
        }

        if (viaA && viaB) {
            if (modoPrioridade == ModoPrioridade.VIA_A) {
                return "Regra aplicada: há veículos nas duas vias, e a configuração atual prioriza a Via A.";
            }

            if (modoPrioridade == ModoPrioridade.VIA_B) {
                return "Regra aplicada: há veículos nas duas vias, e a configuração atual prioriza a Via B.";
            }

            return "Regra aplicada: há veículos nas duas vias, então o sistema alterna a prioridade entre A e B.";
        }

        if (viaA) {
            return "Regra aplicada: há veículos apenas na via A, então a Via A recebe verde.";
        }

        if (viaB) {
            return "Regra aplicada: há veículos apenas na via B, então a Via B recebe verde.";
        }

        return "Regra aplicada: sem veículos e sem pedestres, o sistema mantém a Via A como padrão.";
    }
}