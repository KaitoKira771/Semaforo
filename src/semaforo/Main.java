package semaforo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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

    private final ControladorSemaforo controlador = new ControladorSemaforo();
    private EstadoSemaforo estadoAtual = EstadoSemaforo.A_VERDE;

    private final Color COR_DESLIGADA = new Color(60, 60, 60);
    private final Color COR_FUNDO_CLARO = new Color(245, 245, 245);
    private final Color COR_PAINEL_CLARO = new Color(238, 238, 238);
    private final Color COR_TITULO = new Color(25, 25, 25);
    private final Color COR_CARCACA = new Color(30, 30, 30);

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

    private JPanel aVermelho;
    private JPanel aAmarelo;
    private JPanel aVerde;

    private JPanel bVermelho;
    private JPanel bAmarelo;
    private JPanel bVerde;

    private JPanel pVermelho;
    private JPanel pVerde;

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
        setTitle("Sem\u00E1foro Inteligente");
        setSize(1360, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

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
        painelTitulo = new JPanel(new BorderLayout());
        painelTitulo.setBorder(new EmptyBorder(12, 20, 12, 20));

        lblTitulo = new JLabel("Sistema de Sem\u00E1foro Inteligente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        painelTitulo.add(lblTitulo, BorderLayout.CENTER);
        return painelTitulo;
    }

    private JPanel criarCentro() {
        painelCentro = new JPanel(new BorderLayout(12, 12));
        painelCentro.setBorder(new EmptyBorder(0, 12, 12, 0));
        painelCentro.add(criarPainelSemaforos(), BorderLayout.CENTER);
        painelCentro.add(criarPainelInfo(), BorderLayout.SOUTH);
        return painelCentro;
    }

    private JPanel criarPainelSemaforos() {
        painelSemaforos = new JPanel(new GridLayout(1, 3, 12, 12));
        painelSemaforos.add(criarSemaforoViaA());
        painelSemaforos.add(criarSemaforoViaB());
        painelSemaforos.add(criarSemaforoPedestre());
        return painelSemaforos;
    }

    private JPanel criarSemaforoViaA() {
        painelViaA = criarPainelSemaforoBase("Via A");

        JPanel carcaca = criarCarcacaSemaforo(3);
        aVermelho = criarLuz();
        aAmarelo = criarLuz();
        aVerde = criarLuz();

        carcaca.add(aVermelho);
        carcaca.add(Box.createRigidArea(new Dimension(0, 10)));
        carcaca.add(aAmarelo);
        carcaca.add(Box.createRigidArea(new Dimension(0, 10)));
        carcaca.add(aVerde);

        painelViaA.add(Box.createVerticalGlue());
        painelViaA.add(carcaca);
        painelViaA.add(Box.createVerticalGlue());

        return painelViaA;
    }

    private JPanel criarSemaforoViaB() {
        painelViaB = criarPainelSemaforoBase("Via B");

        JPanel carcaca = criarCarcacaSemaforo(3);
        bVermelho = criarLuz();
        bAmarelo = criarLuz();
        bVerde = criarLuz();

        carcaca.add(bVermelho);
        carcaca.add(Box.createRigidArea(new Dimension(0, 10)));
        carcaca.add(bAmarelo);
        carcaca.add(Box.createRigidArea(new Dimension(0, 10)));
        carcaca.add(bVerde);

        painelViaB.add(Box.createVerticalGlue());
        painelViaB.add(carcaca);
        painelViaB.add(Box.createVerticalGlue());

        return painelViaB;
    }

    private JPanel criarSemaforoPedestre() {
        painelPedestre = criarPainelSemaforoBase("Pedestre");

        JPanel carcaca = criarCarcacaSemaforo(2);
        pVermelho = criarLuz();
        pVerde = criarLuz();

        carcaca.add(pVermelho);
        carcaca.add(Box.createRigidArea(new Dimension(0, 10)));
        carcaca.add(pVerde);

        painelPedestre.add(Box.createVerticalGlue());
        painelPedestre.add(carcaca);
        painelPedestre.add(Box.createVerticalGlue());

        return painelPedestre;
    }

    private JPanel criarPainelSemaforoBase(String titulo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(criarBordaTitulo(titulo));
        painel.setPreferredSize(new Dimension(300, 430));
        return painel;
    }

    private JPanel criarCarcacaSemaforo(int quantidadeLuzes) {
        JPanel carcaca = new JPanel();
        carcaca.setLayout(new BoxLayout(carcaca, BoxLayout.Y_AXIS));
        carcaca.setBackground(COR_CARCACA);
        carcaca.setOpaque(true);
        carcaca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        int altura = quantidadeLuzes == 3 ? 332 : 218;
        carcaca.setMaximumSize(new Dimension(122, altura));
        carcaca.setPreferredSize(new Dimension(122, altura));
        carcaca.setAlignmentX(Component.CENTER_ALIGNMENT);

        return carcaca;
    }

    private JPanel criarLuz() {
        JPanel luz = new JPanel();
        luz.setPreferredSize(new Dimension(90, 90));
        luz.setMaximumSize(new Dimension(90, 90));
        luz.setMinimumSize(new Dimension(90, 90));
        luz.setBackground(COR_DESLIGADA);
        luz.setOpaque(true);
        luz.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        luz.setAlignmentX(Component.CENTER_ALIGNMENT);
        return luz;
    }

    private JPanel criarPainelInfo() {
        painelInfo = new JPanel(new GridLayout(6, 1, 4, 4));
        painelInfo.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Informa\u00E7\u00F5es do Sistema"),
            new EmptyBorder(8, 10, 8, 10)
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
        painelControle = new JPanel(new BorderLayout(10, 10));
        painelControle.setPreferredSize(new Dimension(360, 700));
        painelControle.setBorder(new EmptyBorder(0, 0, 12, 12));

        painelCampos = new JPanel();
        painelCampos.setLayout(new BoxLayout(painelCampos, BoxLayout.Y_AXIS));
        painelCampos.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Controles"),
            new EmptyBorder(10, 10, 10, 10)
        ));

        Font fontePadrao = new Font("Arial", Font.PLAIN, 15);
        Font fonteLabel = new Font("Arial", Font.BOLD, 15);

        chkViaA = new JCheckBox("H\u00E1 ve\u00EDculo na via A");
        chkViaB = new JCheckBox("H\u00E1 ve\u00EDculo na via B");
        chkPedestre = new JCheckBox("H\u00E1 pedestre aguardando");
        chkEmergencia = new JCheckBox("Modo emerg\u00EAncia");
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
        cmbModoOperacao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        lblPrioridade = new JLabel("Prioridade das vias:");
        lblPrioridade.setFont(fonteLabel);

        cmbPrioridade = new JComboBox<>(ControladorSemaforo.ModoPrioridade.values());
        cmbPrioridade.setFont(fontePadrao);
        cmbPrioridade.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        btnSimular = new JButton("Simular ciclo");
        btnAuto = new JButton("Iniciar autom\u00E1tico");
        btnResetar = new JButton("Resetar sistema");

        btnSimular.setFont(new Font("Arial", Font.BOLD, 14));
        btnAuto.setFont(new Font("Arial", Font.BOLD, 14));
        btnResetar.setFont(new Font("Arial", Font.BOLD, 14));

        btnSimular.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAuto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnResetar.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnSimular.addActionListener(e -> simularCiclo());
        btnAuto.addActionListener(e -> alternarModoAutomatico());
        btnResetar.addActionListener(e -> resetarSistema());

        painelCampos.add(chkViaA);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 6)));
        painelCampos.add(chkViaB);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 6)));
        painelCampos.add(chkPedestre);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 6)));
        painelCampos.add(chkEmergencia);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 14)));
        painelCampos.add(lblModoOperacao);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 6)));
        painelCampos.add(cmbModoOperacao);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 14)));
        painelCampos.add(lblPrioridade);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 6)));
        painelCampos.add(cmbPrioridade);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 14)));
        painelCampos.add(chkModoNoturno);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 18)));
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
        txtHistorico.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollHistorico = new JScrollPane(txtHistorico);

        painelHistorico = new JPanel(new BorderLayout());
        painelHistorico.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Hist\u00F3rico"),
            new EmptyBorder(6, 6, 6, 6)
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

        lblRegra.setText("Regra aplicada: " + controlador.explicarRegra(viaA, viaB, pedestre, emergencia));

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
        registrarHistorico("Estado alterado para " + estadoAtual.name() + " | Bin\u00E1rio: " + estadoAtual.getBinario());

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
            btnAuto.setText("Parar autom\u00E1tico");
            btnSimular.setEnabled(false);
            registrarHistorico("Modo autom\u00E1tico ativado.");

            if (!animando) {
                simularCiclo();
            }
        } else {
            btnAuto.setText("Iniciar autom\u00E1tico");
            btnSimular.setEnabled(true);

            if (timerProximoCiclo != null) {
                timerProximoCiclo.stop();
            }

            registrarHistorico("Modo autom\u00E1tico desativado.");
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

        btnAuto.setText("Iniciar autom\u00E1tico");
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
        aAmarelo.setBackground(Color.YELLOW);
        bAmarelo.setBackground(Color.YELLOW);
        pVermelho.setBackground(Color.RED);

        timerPiscaEmergencia = new Timer(500, e -> {
            amareloPiscaLigado = !amareloPiscaLigado;

            if (amareloPiscaLigado) {
                aAmarelo.setBackground(Color.YELLOW);
                bAmarelo.setBackground(Color.YELLOW);
            } else {
                aAmarelo.setBackground(COR_DESLIGADA);
                bAmarelo.setBackground(COR_DESLIGADA);
            }

            pVermelho.setBackground(Color.RED);
        });

        timerPiscaEmergencia.start();
    }

    private void pararPiscaEmergencia() {
        if (timerPiscaEmergencia != null) {
            timerPiscaEmergencia.stop();
        }
        amareloPiscaLigado = false;
    }

    private void atualizarTela() {
        if (estadoAtual != EstadoSemaforo.EMERGENCIA) {
            pararPiscaEmergencia();
        }

        desligarTodasAsLuzes();

        switch (estadoAtual) {
            case A_VERDE:
                aVerde.setBackground(Color.GREEN);
                bVermelho.setBackground(Color.RED);
                pVermelho.setBackground(Color.RED);
                break;

            case A_AMARELO:
                aAmarelo.setBackground(Color.YELLOW);
                bVermelho.setBackground(Color.RED);
                pVermelho.setBackground(Color.RED);
                break;

            case B_VERDE:
                aVermelho.setBackground(Color.RED);
                bVerde.setBackground(Color.GREEN);
                pVermelho.setBackground(Color.RED);
                break;

            case B_AMARELO:
                aVermelho.setBackground(Color.RED);
                bAmarelo.setBackground(Color.YELLOW);
                pVermelho.setBackground(Color.RED);
                break;

            case AB_VERDE:
                aVerde.setBackground(Color.GREEN);
                bVerde.setBackground(Color.GREEN);
                pVermelho.setBackground(Color.RED);
                break;

            case AB_AMARELO:
                aAmarelo.setBackground(Color.YELLOW);
                bAmarelo.setBackground(Color.YELLOW);
                pVermelho.setBackground(Color.RED);
                break;

            case PEDESTRE:
                aVermelho.setBackground(Color.RED);
                bVermelho.setBackground(Color.RED);
                pVerde.setBackground(Color.GREEN);
                break;

            case EMERGENCIA:
                pVermelho.setBackground(Color.RED);
                iniciarPiscaEmergencia();
                break;
        }

        lblEstado.setText("Estado atual: " + estadoAtual.name());
        lblBinario.setText("Bin\u00E1rio: " + estadoAtual.getBinario() + "  [A_V, A_A, A_R, B_V, B_A, B_R, P_V]");
        lblDescricao.setText("Descri\u00E7\u00E3o: " + estadoAtual.getDescricao());
        lblTempos.setText(controlador.getDescricaoTempos());
    }

    private void desligarTodasAsLuzes() {
        aVermelho.setBackground(COR_DESLIGADA);
        aAmarelo.setBackground(COR_DESLIGADA);
        aVerde.setBackground(COR_DESLIGADA);

        bVermelho.setBackground(COR_DESLIGADA);
        bAmarelo.setBackground(COR_DESLIGADA);
        bVerde.setBackground(COR_DESLIGADA);

        pVermelho.setBackground(COR_DESLIGADA);
        pVerde.setBackground(COR_DESLIGADA);
    }

    private void registrarHistorico(String mensagem) {
        txtHistorico.append(formatoHora.format(new Date()) + " - " + mensagem + "\n");
        txtHistorico.setCaretPosition(txtHistorico.getDocument().getLength());
    }

    private TitledBorder criarBordaTitulo(String titulo) {
        Color corLinha;
        Color corTexto;

        if (chkModoNoturno != null && chkModoNoturno.isSelected()) {
            corLinha = new Color(180, 180, 180);
            corTexto = Color.WHITE;
        } else {
            corLinha = new Color(120, 120, 120);
            corTexto = Color.BLACK;
        }

        TitledBorder borda = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(corLinha),
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

        if (noturno) {
            fundoJanela = new Color(24, 24, 24);
            fundoPainel = new Color(40, 40, 40);
            fundoSecundario = new Color(55, 55, 55);
            texto = Color.WHITE;
            fundoTitulo = new Color(18, 18, 18);
        } else {
            fundoJanela = COR_FUNDO_CLARO;
            fundoPainel = COR_PAINEL_CLARO;
            fundoSecundario = Color.WHITE;
            texto = Color.BLACK;
            fundoTitulo = COR_TITULO;
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

        if (cmbModoOperacao != null) {
            cmbModoOperacao.setBackground(fundoSecundario);
            cmbModoOperacao.setForeground(texto);
        }

        if (cmbPrioridade != null) {
            cmbPrioridade.setBackground(fundoSecundario);
            cmbPrioridade.setForeground(texto);
        }

        configurarBotao(btnSimular, fundoSecundario, texto);
        configurarBotao(btnAuto, fundoSecundario, texto);
        configurarBotao(btnResetar, fundoSecundario, texto);

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

    private void configurarBotao(JButton botao, Color fundo, Color texto) {
        if (botao != null) {
            botao.setBackground(fundo);
            botao.setForeground(texto);
            botao.setFocusPainted(false);
            botao.setMaximumSize(new Dimension(180, 36));
            botao.setPreferredSize(new Dimension(180, 36));
        }
    }

    private void atualizarBordas() {
        if (painelViaA != null) painelViaA.setBorder(criarBordaTitulo("Via A"));
        if (painelViaB != null) painelViaB.setBorder(criarBordaTitulo("Via B"));
        if (painelPedestre != null) painelPedestre.setBorder(criarBordaTitulo("Pedestre"));
        if (painelInfo != null) painelInfo.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Informa\u00E7\u00F5es do Sistema"),
            new EmptyBorder(8, 10, 8, 10)
        ));
        if (painelCampos != null) painelCampos.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Controles"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        if (painelHistorico != null) painelHistorico.setBorder(BorderFactory.createCompoundBorder(
            criarBordaTitulo("Hist\u00F3rico"),
            new EmptyBorder(6, 6, 6, 6)
        ));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main tela = new Main();
            tela.setVisible(true);
        });
    }
}