package semaforo;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class Main extends JFrame {

    /*
     * =========================================================
     * ÁREA DE EDIÇÃO RÁPIDA - VISUAL E LAYOUT
     * =========================================================
     * Aqui ficam as mudanças mais fáceis do projeto.
     *
     * Você pode alterar aqui:
     * - tamanho da janela
     * - tamanho das luzes
     * - espaço entre as luzes
     * - velocidade do pisca da emergência
     * - cores principais
     */
    private static final int LARGURA_JANELA = 1380;
    private static final int ALTURA_JANELA = 820;

    private static final int TAMANHO_LUZ = 92;
    private static final int ESPACO_LUZES = 12;
    private static final int INTERVALO_PISCA_EMERGENCIA = 450;

    private final Color COR_DESLIGADA = new Color(70, 74, 82);
    private final Color COR_VERMELHO = new Color(239, 68, 68);
    private final Color COR_AMARELO = new Color(250, 204, 21);
    private final Color COR_VERDE = new Color(34, 197, 94);

    private final ControladorSemaforo controlador = new ControladorSemaforo();
    private EstadoSemaforo estadoAtual = EstadoSemaforo.A_VERDE;

    private boolean modoAutomatico = false;
    private boolean animando = false;
    private boolean amareloPiscaLigado = false;

    private int segundosRestantes = 0;

    private Timer timerContagem;
    private Timer timerEtapa;
    private Timer timerProximoCiclo;
    private Timer timerPiscaEmergencia;

    private final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

    private JPanel painelTitulo;
    private JPanel painelCentro;
    private JPanel painelSemaforos;
    private JPanel painelControle;
    private JPanel painelInfo;
    private JPanel painelViaA;
    private JPanel painelViaB;
    private JPanel painelPedestre;
    private JPanel painelHistorico;
    private JPanel painelCampos;

    private JLabel lblTitulo;
    private JLabel lblSubtitulo;

    private LuzCircular aVermelho;
    private LuzCircular aAmarelo;
    private LuzCircular aVerde;

    private LuzCircular bVermelho;
    private LuzCircular bAmarelo;
    private LuzCircular bVerde;

    private LuzCircular pVermelho;
    private LuzCircular pVerde;

    private JCheckBox chkViaA;
    private JCheckBox chkViaB;
    private JCheckBox chkPedestre;
    private JCheckBox chkEmergencia;
    private JCheckBox chkModoNoturno;

    private JLabel lblModoOperacao;
    private JLabel lblPrioridade;

    private JComboBox<ControladorSemaforo.ModoOperacao> cmbModoOperacao;
    private JComboBox<ControladorSemaforo.ModoPrioridade> cmbPrioridade;

    private JButton btnSimular;
    private JButton btnAuto;
    private JButton btnResetar;

    private JLabel lblEstado;
    private JLabel lblBinario;
    private JLabel lblDescricao;
    private JLabel lblRegra;
    private JLabel lblContagem;
    private JLabel lblTempos;

    private JTextArea txtHistorico;
    private JScrollPane scrollHistorico;

    public Main() {
        setTitle("Semáforo Inteligente");
        setSize(LARGURA_JANELA, ALTURA_JANELA);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(14, 14));

        add(criarTitulo(), BorderLayout.NORTH);
        add(criarCentro(), BorderLayout.CENTER);
        add(criarPainelControle(), BorderLayout.EAST);

        atualizarTela();
        lblRegra.setText("Regra aplicada: sistema iniciado.");
        lblContagem.setText("Contagem: --");
        registrarHistorico("Sistema iniciado no estado " + estadoAtual.name() + ".");
        aplicarTema();
    }

    private JPanel criarTitulo() {
        painelTitulo = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(31, 41, 55),
                    getWidth(), getHeight(), new Color(17, 24, 39)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        painelTitulo.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel conteudo = new JPanel();
        conteudo.setOpaque(false);
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));

        lblTitulo = new JLabel("Sistema de Semáforo Inteligente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSubtitulo = new JLabel("Simulação visual com estados, tempo, prioridade e emergência", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(226, 232, 240));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        conteudo.add(lblTitulo);
        conteudo.add(Box.createRigidArea(new Dimension(0, 6)));
        conteudo.add(lblSubtitulo);

        painelTitulo.add(conteudo, BorderLayout.CENTER);
        return painelTitulo;
    }

    private JPanel criarCentro() {
        painelCentro = new JPanel(new BorderLayout(14, 14));
        painelCentro.setBorder(new EmptyBorder(0, 14, 14, 0));
        painelCentro.add(criarPainelSemaforos(), BorderLayout.CENTER);
        painelCentro.add(criarPainelInfo(), BorderLayout.SOUTH);
        return painelCentro;
    }

    private JPanel criarPainelSemaforos() {
        painelSemaforos = new JPanel(new GridLayout(1, 3, 14, 14));
        painelSemaforos.add(criarSemaforoViaA());
        painelSemaforos.add(criarSemaforoViaB());
        painelSemaforos.add(criarSemaforoPedestre());
        return painelSemaforos;
    }

    /*
     * =========================================================
     * EDIÇÃO FÁCIL - QUANTIDADE DE SEMÁFOROS / LUZES
     * =========================================================
     * Se você quiser adicionar ou remover luzes, os principais
     * pontos para mexer são estes métodos:
     *
     * - criarSemaforoViaA()
     * - criarSemaforoViaB()
     * - criarSemaforoPedestre()
     * - atualizarTela()
     * - desligarTodasAsLuzes()
     *
     * Exemplo:
     * se quiser um novo grupo de semáforo, crie um novo painel
     * parecido com Via A / Via B / Pedestre.
     */
    private JPanel criarSemaforoViaA() {
        painelViaA = criarCardSemaforo("Via A");

        JPanel carcaca = criarCarcacaSemaforo(3);
        aVermelho = new LuzCircular();
        aAmarelo = new LuzCircular();
        aVerde = new LuzCircular();

        carcaca.add(aVermelho);
        carcaca.add(Box.createRigidArea(new Dimension(0, ESPACO_LUZES)));
        carcaca.add(aAmarelo);
        carcaca.add(Box.createRigidArea(new Dimension(0, ESPACO_LUZES)));
        carcaca.add(aVerde);

        painelViaA.add(Box.createVerticalGlue());
        painelViaA.add(carcaca);
        painelViaA.add(Box.createVerticalGlue());

        return painelViaA;
    }

    private JPanel criarSemaforoViaB() {
        painelViaB = criarCardSemaforo("Via B");

        JPanel carcaca = criarCarcacaSemaforo(3);
        bVermelho = new LuzCircular();
        bAmarelo = new LuzCircular();
        bVerde = new LuzCircular();

        carcaca.add(bVermelho);
        carcaca.add(Box.createRigidArea(new Dimension(0, ESPACO_LUZES)));
        carcaca.add(bAmarelo);
        carcaca.add(Box.createRigidArea(new Dimension(0, ESPACO_LUZES)));
        carcaca.add(bVerde);

        painelViaB.add(Box.createVerticalGlue());
        painelViaB.add(carcaca);
        painelViaB.add(Box.createVerticalGlue());

        return painelViaB;
    }

    private JPanel criarSemaforoPedestre() {
        painelPedestre = criarCardSemaforo("Pedestre");

        JPanel carcaca = criarCarcacaSemaforo(2);
        pVermelho = new LuzCircular();
        pVerde = new LuzCircular();

        carcaca.add(pVermelho);
        carcaca.add(Box.createRigidArea(new Dimension(0, ESPACO_LUZES)));
        carcaca.add(pVerde);

        painelPedestre.add(Box.createVerticalGlue());
        painelPedestre.add(carcaca);
        painelPedestre.add(Box.createVerticalGlue());

        return painelPedestre;
    }

    private JPanel criarCardSemaforo(String titulo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo(titulo),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return painel;
    }

    private JPanel criarCarcacaSemaforo(int quantidadeLuzes) {
        JPanel carcaca = new JPanel();
        carcaca.setLayout(new BoxLayout(carcaca, BoxLayout.Y_AXIS));
        carcaca.setBackground(new Color(24, 24, 27));
        carcaca.setOpaque(true);
        carcaca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(15, 23, 42), 2),
            new EmptyBorder(16, 16, 16, 16)
        ));

        int altura = quantidadeLuzes == 3 ? 360 : 238;
        carcaca.setMaximumSize(new Dimension(132, altura));
        carcaca.setPreferredSize(new Dimension(132, altura));
        carcaca.setAlignmentX(Component.CENTER_ALIGNMENT);

        return carcaca;
    }

    private JPanel criarPainelInfo() {
        painelInfo = new JPanel(new GridLayout(6, 1, 6, 6));
        painelInfo.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Informações do Sistema"),
            new EmptyBorder(12, 14, 12, 14)
        ));

        Font fonteInfo = new Font("Arial", Font.PLAIN, 15);

        lblEstado = new JLabel();
        lblBinario = new JLabel();
        lblDescricao = new JLabel();
        lblRegra = new JLabel();
        lblContagem = new JLabel();
        lblTempos = new JLabel();

        lblEstado.setFont(fonteInfo);
        lblBinario.setFont(fonteInfo);
        lblDescricao.setFont(fonteInfo);
        lblRegra.setFont(fonteInfo);
        lblContagem.setFont(fonteInfo);
        lblTempos.setFont(fonteInfo);

        painelInfo.add(lblEstado);
        painelInfo.add(lblBinario);
        painelInfo.add(lblDescricao);
        painelInfo.add(lblRegra);
        painelInfo.add(lblContagem);
        painelInfo.add(lblTempos);

        return painelInfo;
    }

    private JPanel criarPainelControle() {
        painelControle = new JPanel(new BorderLayout(12, 12));
        painelControle.setPreferredSize(new Dimension(380, 730));
        painelControle.setBorder(new EmptyBorder(0, 0, 14, 14));

        painelCampos = new JPanel();
        painelCampos.setLayout(new BoxLayout(painelCampos, BoxLayout.Y_AXIS));
        painelCampos.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Controles"),
            new EmptyBorder(14, 14, 14, 14)
        ));

        Font fontePadrao = new Font("Arial", Font.PLAIN, 15);
        Font fonteLabel = new Font("Arial", Font.BOLD, 15);

        chkViaA = new JCheckBox("Há veículo na via A");
        chkViaB = new JCheckBox("Há veículo na via B");
        chkPedestre = new JCheckBox("Há pedestre aguardando");
        chkEmergencia = new JCheckBox("Modo emergência");
        chkModoNoturno = new JCheckBox("Modo noturno");

        chkViaA.setFont(fontePadrao);
        chkViaB.setFont(fontePadrao);
        chkPedestre.setFont(fontePadrao);
        chkEmergencia.setFont(fontePadrao);
        chkModoNoturno.setFont(fontePadrao);

        chkModoNoturno.addActionListener(e -> aplicarTema());

        lblModoOperacao = new JLabel("Modo do sistema:");
        lblModoOperacao.setFont(fonteLabel);

        cmbModoOperacao = new JComboBox<>(ControladorSemaforo.ModoOperacao.values());
        cmbModoOperacao.setFont(fontePadrao);
        cmbModoOperacao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        lblPrioridade = new JLabel("Prioridade das vias:");
        lblPrioridade.setFont(fonteLabel);

        cmbPrioridade = new JComboBox<>(ControladorSemaforo.ModoPrioridade.values());
        cmbPrioridade.setFont(fontePadrao);
        cmbPrioridade.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        btnSimular = new JButton("Simular ciclo");
        btnAuto = new JButton("Iniciar automático");
        btnResetar = new JButton("Resetar sistema");

        configurarBotao(btnSimular);
        configurarBotao(btnAuto);
        configurarBotao(btnResetar);

        btnSimular.addActionListener(e -> simularCiclo());
        btnAuto.addActionListener(e -> alternarModoAutomatico());
        btnResetar.addActionListener(e -> resetarSistema());

        painelCampos.add(chkViaA);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 8)));
        painelCampos.add(chkViaB);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 8)));
        painelCampos.add(chkPedestre);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 8)));
        painelCampos.add(chkEmergencia);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 18)));
        painelCampos.add(lblModoOperacao);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 6)));
        painelCampos.add(cmbModoOperacao);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 16)));
        painelCampos.add(lblPrioridade);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 6)));
        painelCampos.add(cmbPrioridade);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 16)));
        painelCampos.add(chkModoNoturno);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 20)));
        painelCampos.add(btnSimular);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 10)));
        painelCampos.add(btnAuto);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 10)));
        painelCampos.add(btnResetar);

        txtHistorico = new JTextArea(16, 24);
        txtHistorico.setEditable(false);
        txtHistorico.setLineWrap(true);
        txtHistorico.setWrapStyleWord(true);
        txtHistorico.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtHistorico.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollHistorico = new JScrollPane(txtHistorico);
        scrollHistorico.setBorder(BorderFactory.createEmptyBorder());

        painelHistorico = new JPanel(new BorderLayout());
        painelHistorico.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Histórico"),
            new EmptyBorder(8, 8, 8, 8)
        ));
        painelHistorico.add(scrollHistorico, BorderLayout.CENTER);

        painelControle.add(painelCampos, BorderLayout.NORTH);
        painelControle.add(painelHistorico, BorderLayout.CENTER);

        return painelControle;
    }

    private void simularCiclo() {
        if (animando) {
            return;
        }

        controlador.setModoOperacao((ControladorSemaforo.ModoOperacao) cmbModoOperacao.getSelectedItem());
        controlador.setModoPrioridade((ControladorSemaforo.ModoPrioridade) cmbPrioridade.getSelectedItem());

        boolean viaA = chkViaA.isSelected();
        boolean viaB = chkViaB.isSelected();
        boolean pedestre = chkPedestre.isSelected();
        boolean emergencia = chkEmergencia.isSelected();

        lblRegra.setText(controlador.explicarRegra(viaA, viaB, pedestre, emergencia));

        List<EstadoSemaforo> sequencia = controlador.montarSequencia(estadoAtual, viaA, viaB, pedestre, emergencia);

        animando = true;
        btnSimular.setEnabled(false);

        animarSequencia(sequencia, 0);
    }

    private void animarSequencia(final List<EstadoSemaforo> sequencia, final int indice) {
        if (indice >= sequencia.size()) {
            animando = false;

            if (!modoAutomatico) {
                btnSimular.setEnabled(true);
            } else {
                agendarProximoCicloAutomatico();
            }
            return;
        }

        estadoAtual = sequencia.get(indice);
        atualizarTela();
        registrarHistorico("Estado alterado para " + estadoAtual.name() + " | Binário: " + estadoAtual.getBinario());

        final int duracao = controlador.getDuracaoEstado(estadoAtual);
        iniciarContagemRegressiva(duracao);

        timerEtapa = new Timer(duracao * 1000, e -> {
            ((Timer) e.getSource()).stop();
            animarSequencia(sequencia, indice + 1);
        });

        timerEtapa.setRepeats(false);
        timerEtapa.start();
    }

    private void iniciarContagemRegressiva(int segundos) {
        if (timerContagem != null) {
            timerContagem.stop();
        }

        segundosRestantes = segundos;
        lblContagem.setText("Contagem: " + segundosRestantes + " s");

        timerContagem = new Timer(1000, e -> {
            segundosRestantes--;

            if (segundosRestantes > 0) {
                lblContagem.setText("Contagem: " + segundosRestantes + " s");
            } else {
                lblContagem.setText("Contagem: 0 s");
                ((Timer) e.getSource()).stop();
            }
        });

        timerContagem.start();
    }

    private void agendarProximoCicloAutomatico() {
        if (!modoAutomatico) {
            btnSimular.setEnabled(true);
            return;
        }

        timerProximoCiclo = new Timer(1000, e -> {
            ((Timer) e.getSource()).stop();
            simularCiclo();
        });

        timerProximoCiclo.setRepeats(false);
        timerProximoCiclo.start();
    }

    private void alternarModoAutomatico() {
        modoAutomatico = !modoAutomatico;

        if (modoAutomatico) {
            btnAuto.setText("Parar automático");
            btnSimular.setEnabled(false);
            registrarHistorico("Modo automático ativado.");

            if (!animando) {
                simularCiclo();
            }
        } else {
            btnAuto.setText("Iniciar automático");
            btnSimular.setEnabled(true);

            if (timerProximoCiclo != null) {
                timerProximoCiclo.stop();
            }

            registrarHistorico("Modo automático desativado.");
        }
    }

    private void resetarSistema() {
        pararTimers();
        pararPiscaEmergencia();

        modoAutomatico = false;
        animando = false;

        controlador.resetar();
        estadoAtual = EstadoSemaforo.A_VERDE;

        chkViaA.setSelected(false);
        chkViaB.setSelected(false);
        chkPedestre.setSelected(false);
        chkEmergencia.setSelected(false);
        cmbModoOperacao.setSelectedItem(ControladorSemaforo.ModoOperacao.CRUZAMENTO);
        cmbPrioridade.setSelectedItem(ControladorSemaforo.ModoPrioridade.ALTERNADA);

        btnAuto.setText("Iniciar automático");
        btnSimular.setEnabled(true);

        txtHistorico.setText("");
        lblRegra.setText("Regra aplicada: sistema resetado.");
        lblContagem.setText("Contagem: --");

        atualizarTela();
        registrarHistorico("Sistema resetado para o estado inicial.");
    }

    private void pararTimers() {
        if (timerContagem != null) {
            timerContagem.stop();
        }

        if (timerEtapa != null) {
            timerEtapa.stop();
        }

        if (timerProximoCiclo != null) {
            timerProximoCiclo.stop();
        }
    }

    private void iniciarPiscaEmergencia() {
        if (timerPiscaEmergencia != null && timerPiscaEmergencia.isRunning()) {
            return;
        }

        amareloPiscaLigado = true;
        aAmarelo.setCor(COR_AMARELO);
        bAmarelo.setCor(COR_AMARELO);
        pVermelho.setCor(COR_VERMELHO);

        timerPiscaEmergencia = new Timer(INTERVALO_PISCA_EMERGENCIA, e -> {
            amareloPiscaLigado = !amareloPiscaLigado;

            if (amareloPiscaLigado) {
                aAmarelo.setCor(COR_AMARELO);
                bAmarelo.setCor(COR_AMARELO);
            } else {
                aAmarelo.setCor(COR_DESLIGADA);
                bAmarelo.setCor(COR_DESLIGADA);
            }

            pVermelho.setCor(COR_VERMELHO);
        });

        timerPiscaEmergencia.start();
    }

    private void pararPiscaEmergencia() {
        if (timerPiscaEmergencia != null) {
            timerPiscaEmergencia.stop();
        }
        amareloPiscaLigado = false;
    }

    /*
     * =========================================================
     * EDIÇÃO FÁCIL - CORES DAS LUZES
     * =========================================================
     * Se quiser trocar as cores do semáforo, mexa principalmente:
     * - COR_VERMELHO
     * - COR_AMARELO
     * - COR_VERDE
     * - COR_DESLIGADA
     *
     * E também neste método, caso queira mudar a lógica visual.
     */
    private void atualizarTela() {
        if (estadoAtual != EstadoSemaforo.EMERGENCIA) {
            pararPiscaEmergencia();
        }

        desligarTodasAsLuzes();

        switch (estadoAtual) {
            case A_VERDE:
                aVerde.setCor(COR_VERDE);
                bVermelho.setCor(COR_VERMELHO);
                pVermelho.setCor(COR_VERMELHO);
                break;

            case A_AMARELO:
                aAmarelo.setCor(COR_AMARELO);
                bVermelho.setCor(COR_VERMELHO);
                pVermelho.setCor(COR_VERMELHO);
                break;

            case B_VERDE:
                aVermelho.setCor(COR_VERMELHO);
                bVerde.setCor(COR_VERDE);
                pVermelho.setCor(COR_VERMELHO);
                break;

            case B_AMARELO:
                aVermelho.setCor(COR_VERMELHO);
                bAmarelo.setCor(COR_AMARELO);
                pVermelho.setCor(COR_VERMELHO);
                break;

            case AB_VERDE:
                aVerde.setCor(COR_VERDE);
                bVerde.setCor(COR_VERDE);
                pVermelho.setCor(COR_VERMELHO);
                break;

            case AB_AMARELO:
                aAmarelo.setCor(COR_AMARELO);
                bAmarelo.setCor(COR_AMARELO);
                pVermelho.setCor(COR_VERMELHO);
                break;

            case PEDESTRE:
                aVermelho.setCor(COR_VERMELHO);
                bVermelho.setCor(COR_VERMELHO);
                pVerde.setCor(COR_VERDE);
                break;

            case EMERGENCIA:
                pVermelho.setCor(COR_VERMELHO);
                iniciarPiscaEmergencia();
                break;
        }

        lblEstado.setText("Estado atual: " + estadoAtual.name());
        lblBinario.setText("Binário: " + estadoAtual.getBinario() + "  [A_V, A_A, A_R, B_V, B_A, B_R, P_V]");
        lblDescricao.setText("Descrição: " + estadoAtual.getDescricao());
        lblTempos.setText(controlador.getDescricaoTempos());
    }

    private void desligarTodasAsLuzes() {
        aVermelho.setCor(COR_DESLIGADA);
        aAmarelo.setCor(COR_DESLIGADA);
        aVerde.setCor(COR_DESLIGADA);

        bVermelho.setCor(COR_DESLIGADA);
        bAmarelo.setCor(COR_DESLIGADA);
        bVerde.setCor(COR_DESLIGADA);

        pVermelho.setCor(COR_DESLIGADA);
        pVerde.setCor(COR_DESLIGADA);
    }

    private void registrarHistorico(String mensagem) {
        txtHistorico.append(formatoHora.format(new Date()) + " - " + mensagem + "\n");
        txtHistorico.setCaretPosition(txtHistorico.getDocument().getLength());
    }

    private TitledBorder criarBordaTitulo(String titulo) {
        Color corLinha;
        Color corTexto;

        if (chkModoNoturno != null && chkModoNoturno.isSelected()) {
            corLinha = new Color(100, 116, 139);
            corTexto = Color.WHITE;
        } else {
            corLinha = new Color(148, 163, 184);
            corTexto = new Color(30, 41, 59);
        }

        TitledBorder borda = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(corLinha, 1),
            titulo
        );
        borda.setTitleColor(corTexto);
        borda.setTitleFont(new Font("Arial", Font.BOLD, 15));
        return borda;
    }

    private void aplicarTema() {
        boolean noturno = chkModoNoturno != null && chkModoNoturno.isSelected();

        Color fundoJanela;
        Color fundoPainel;
        Color fundoSecundario;
        Color texto;
        Color fundoTitulo;
        Color textoSub;

        if (noturno) {
            fundoJanela = new Color(15, 23, 42);
            fundoPainel = new Color(30, 41, 59);
            fundoSecundario = new Color(51, 65, 85);
            texto = Color.WHITE;
            fundoTitulo = new Color(17, 24, 39);
            textoSub = new Color(203, 213, 225);
        } else {
            fundoJanela = new Color(241, 245, 249);
            fundoPainel = Color.WHITE;
            fundoSecundario = new Color(248, 250, 252);
            texto = new Color(15, 23, 42);
            fundoTitulo = new Color(17, 24, 39);
            textoSub = new Color(226, 232, 240);
        }

        getContentPane().setBackground(fundoJanela);

        if (painelTitulo != null) painelTitulo.setBackground(fundoTitulo);
        if (painelCentro != null) painelCentro.setBackground(fundoJanela);
        if (painelSemaforos != null) painelSemaforos.setBackground(fundoJanela);

        if (painelViaA != null) painelViaA.setBackground(fundoPainel);
        if (painelViaB != null) painelViaB.setBackground(fundoPainel);
        if (painelPedestre != null) painelPedestre.setBackground(fundoPainel);
        if (painelInfo != null) painelInfo.setBackground(fundoPainel);
        if (painelControle != null) painelControle.setBackground(fundoJanela);
        if (painelHistorico != null) painelHistorico.setBackground(fundoPainel);
        if (painelCampos != null) painelCampos.setBackground(fundoPainel);

        if (lblTitulo != null) lblTitulo.setForeground(Color.WHITE);
        if (lblSubtitulo != null) lblSubtitulo.setForeground(textoSub);

        if (lblEstado != null) lblEstado.setForeground(texto);
        if (lblBinario != null) lblBinario.setForeground(texto);
        if (lblDescricao != null) lblDescricao.setForeground(texto);
        if (lblRegra != null) lblRegra.setForeground(texto);
        if (lblContagem != null) lblContagem.setForeground(texto);
        if (lblTempos != null) lblTempos.setForeground(texto);
        if (lblModoOperacao != null) lblModoOperacao.setForeground(texto);
        if (lblPrioridade != null) lblPrioridade.setForeground(texto);

        configurarCheckBox(chkViaA, fundoPainel, texto);
        configurarCheckBox(chkViaB, fundoPainel, texto);
        configurarCheckBox(chkPedestre, fundoPainel, texto);
        configurarCheckBox(chkEmergencia, fundoPainel, texto);
        configurarCheckBox(chkModoNoturno, fundoPainel, texto);

        configurarCombo(cmbModoOperacao, fundoSecundario, texto);
        configurarCombo(cmbPrioridade, fundoSecundario, texto);

        if (txtHistorico != null) {
            txtHistorico.setBackground(fundoSecundario);
            txtHistorico.setForeground(texto);
            txtHistorico.setCaretColor(texto);
        }

        if (scrollHistorico != null && scrollHistorico.getViewport() != null) {
            scrollHistorico.getViewport().setBackground(fundoSecundario);
        }

        atualizarBordas();
        repaint();
        revalidate();
    }

    private void configurarCheckBox(JCheckBox checkBox, Color fundo, Color texto) {
        if (checkBox != null) {
            checkBox.setOpaque(true);
            checkBox.setBackground(fundo);
            checkBox.setForeground(texto);
        }
    }

    private void configurarCombo(JComboBox<?> combo, Color fundo, Color texto) {
        if (combo != null) {
            combo.setBackground(fundo);
            combo.setForeground(texto);
        }
    }

    private void configurarBotao(JButton botao) {
        botao.setFocusPainted(false);
        botao.setAlignmentX(Component.CENTER_ALIGNMENT);
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        botao.setPreferredSize(new Dimension(220, 42));
        botao.setMargin(new Insets(10, 16, 10, 16));
        botao.setBackground(new Color(37, 99, 235));
        botao.setForeground(Color.WHITE);
        botao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(29, 78, 216), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void atualizarBordas() {
        if (painelViaA != null) {
            painelViaA.setBorder(BorderFactory.createCompoundBorder(
                criarBordaTitulo("Via A"),
                new EmptyBorder(16, 16, 16, 16)
            ));
        }

        if (painelViaB != null) {
            painelViaB.setBorder(BorderFactory.createCompoundBorder(
                criarBordaTitulo("Via B"),
                new EmptyBorder(16, 16, 16, 16)
            ));
        }

        if (painelPedestre != null) {
            painelPedestre.setBorder(BorderFactory.createCompoundBorder(
                criarBordaTitulo("Pedestre"),
                new EmptyBorder(16, 16, 16, 16)
            ));
        }

        if (painelInfo != null) {
            painelInfo.setBorder(BorderFactory.createCompoundBorder(
                criarBordaTitulo("Informações do Sistema"),
                new EmptyBorder(12, 14, 12, 14)
            ));
        }

        if (painelCampos != null) {
            painelCampos.setBorder(BorderFactory.createCompoundBorder(
                criarBordaTitulo("Controles"),
                new EmptyBorder(14, 14, 14, 14)
            ));
        }

        if (painelHistorico != null) {
            painelHistorico.setBorder(BorderFactory.createCompoundBorder(
                criarBordaTitulo("Histórico"),
                new EmptyBorder(8, 8, 8, 8)
            ));
        }
    }

    /*
     * =========================================================
     * EDIÇÃO FÁCIL - TAMANHO DAS LUZES
     * =========================================================
     * Para aumentar ou diminuir o tamanho das luzes:
     * altere a constante TAMANHO_LUZ.
     */
    private class LuzCircular extends JPanel {
        private Color corAtual = COR_DESLIGADA;

        public LuzCircular() {
            setOpaque(false);
            setPreferredSize(new Dimension(TAMANHO_LUZ, TAMANHO_LUZ));
            setMaximumSize(new Dimension(TAMANHO_LUZ, TAMANHO_LUZ));
            setMinimumSize(new Dimension(TAMANHO_LUZ, TAMANHO_LUZ));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        public void setCor(Color cor) {
            this.corAtual = cor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margem = 4;
            int tamanho = Math.min(getWidth(), getHeight()) - (margem * 2);

            g2.setColor(new Color(20, 20, 20));
            g2.fillOval(margem - 2, margem - 2, tamanho + 4, tamanho + 4);

            GradientPaint sombra = new GradientPaint(
                0, 0, corAtual.brighter(),
                getWidth(), getHeight(), corAtual.darker()
            );
            g2.setPaint(sombra);
            g2.fillOval(margem, margem, tamanho, tamanho);

            g2.setColor(new Color(255, 255, 255, 90));
            g2.fillOval(margem + 12, margem + 10, tamanho / 3, tamanho / 4);

            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawOval(margem, margem, tamanho, tamanho);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main tela = new Main();
            tela.setVisible(true);
        });
    }
}