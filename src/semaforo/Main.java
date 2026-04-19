package semaforo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.border.TitledBorder;

public class Main extends JFrame {

    private final ControladorSemaforo controlador = new ControladorSemaforo();
    private EstadoSemaforo estadoAtual = EstadoSemaforo.A_VERDE;

    private final Color COR_DESLIGADA = new Color(60, 60, 60);

    private boolean modoAutomatico = false;
    private boolean animando = false;

    private int segundosRestantes = 0;

    private Timer timerContagem;
    private Timer timerEtapa;
    private Timer timerProximoCiclo;

    private final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

    // Painéis principais
    private JPanel painelTitulo;
    private JPanel painelCentro;
    private JPanel painelSemaforos;
    private JPanel painelControle;
    private JPanel painelInfo;
    private JPanel painelViaA;
    private JPanel painelViaB;
    private JPanel painelPedestre;
    private JPanel painelHistorico;

    // Título
    private JLabel lblTitulo;

    // Luzes via A
    private JPanel aVermelho;
    private JPanel aAmarelo;
    private JPanel aVerde;

    // Luzes via B
    private JPanel bVermelho;
    private JPanel bAmarelo;
    private JPanel bVerde;

    // Luzes pedestre
    private JPanel pVermelho;
    private JPanel pVerde;

    // Entradas
    private JCheckBox chkViaA;
    private JCheckBox chkViaB;
    private JCheckBox chkPedestre;
    private JCheckBox chkEmergencia;
    private JCheckBox chkModoNoturno;

    // Prioridade
    private JComboBox<ControladorSemaforo.ModoPrioridade> cmbPrioridade;

    // Botões
    private JButton btnSimular;
    private JButton btnAuto;
    private JButton btnResetar;

    // Informações
    private JLabel lblEstado;
    private JLabel lblBinario;
    private JLabel lblDescricao;
    private JLabel lblRegra;
    private JLabel lblContagem;

    // Histórico
    private JTextArea txtHistorico;
    private JScrollPane scrollHistorico;

    public Main() {
        setTitle("Semáforo Inteligente");
        setSize(1250, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

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
        painelTitulo = new JPanel();

        lblTitulo = new JLabel("Sistema de Semáforo Inteligente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));

        painelTitulo.add(lblTitulo);
        return painelTitulo;
    }

    private JPanel criarCentro() {
        painelCentro = new JPanel(new BorderLayout(10, 10));
        painelCentro.add(criarPainelSemaforos(), BorderLayout.CENTER);
        painelCentro.add(criarPainelInfo(), BorderLayout.SOUTH);
        return painelCentro;
    }

    private JPanel criarPainelSemaforos() {
        painelSemaforos = new JPanel(new GridLayout(1, 3, 20, 20));
        painelSemaforos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        painelSemaforos.add(criarSemaforoViaA());
        painelSemaforos.add(criarSemaforoViaB());
        painelSemaforos.add(criarSemaforoPedestre());

        return painelSemaforos;
    }

    private JPanel criarSemaforoViaA() {
        painelViaA = new JPanel();
        painelViaA.setLayout(new BoxLayout(painelViaA, BoxLayout.Y_AXIS));

        aVermelho = criarLuz();
        aAmarelo = criarLuz();
        aVerde = criarLuz();

        painelViaA.add(Box.createRigidArea(new Dimension(0, 10)));
        painelViaA.add(aVermelho);
        painelViaA.add(aAmarelo);
        painelViaA.add(aVerde);

        return painelViaA;
    }

    private JPanel criarSemaforoViaB() {
        painelViaB = new JPanel();
        painelViaB.setLayout(new BoxLayout(painelViaB, BoxLayout.Y_AXIS));

        bVermelho = criarLuz();
        bAmarelo = criarLuz();
        bVerde = criarLuz();

        painelViaB.add(Box.createRigidArea(new Dimension(0, 10)));
        painelViaB.add(bVermelho);
        painelViaB.add(bAmarelo);
        painelViaB.add(bVerde);

        return painelViaB;
    }

    private JPanel criarSemaforoPedestre() {
        painelPedestre = new JPanel();
        painelPedestre.setLayout(new BoxLayout(painelPedestre, BoxLayout.Y_AXIS));

        pVermelho = criarLuz();
        pVerde = criarLuz();

        painelPedestre.add(Box.createRigidArea(new Dimension(0, 10)));
        painelPedestre.add(pVermelho);
        painelPedestre.add(pVerde);

        return painelPedestre;
    }

    private JPanel criarLuz() {
        JPanel luz = new JPanel();
        luz.setPreferredSize(new Dimension(90, 90));
        luz.setMaximumSize(new Dimension(90, 90));
        luz.setBackground(COR_DESLIGADA);
        luz.setOpaque(true);
        luz.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return luz;
    }

    private JPanel criarPainelInfo() {
        painelInfo = new JPanel(new GridLayout(5, 1, 5, 5));

        lblEstado = new JLabel();
        lblBinario = new JLabel();
        lblDescricao = new JLabel();
        lblRegra = new JLabel();
        lblContagem = new JLabel();

        painelInfo.add(lblEstado);
        painelInfo.add(lblBinario);
        painelInfo.add(lblDescricao);
        painelInfo.add(lblRegra);
        painelInfo.add(lblContagem);

        return painelInfo;
    }

    private JPanel criarPainelControle() {
        painelControle = new JPanel(new BorderLayout(10, 10));
        painelControle.setPreferredSize(new Dimension(340, 650));

        JPanel painelCampos = new JPanel();
        painelCampos.setLayout(new BoxLayout(painelCampos, BoxLayout.Y_AXIS));

        chkViaA = new JCheckBox("Há veículo na via A");
        chkViaB = new JCheckBox("Há veículo na via B");
        chkPedestre = new JCheckBox("Há pedestre aguardando");
        chkEmergencia = new JCheckBox("Modo emergência");
        chkModoNoturno = new JCheckBox("Modo noturno");

        chkModoNoturno.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aplicarTema();
            }
        });

        cmbPrioridade = new JComboBox<ControladorSemaforo.ModoPrioridade>(ControladorSemaforo.ModoPrioridade.values());

        btnSimular = new JButton("Simular ciclo");
        btnAuto = new JButton("Iniciar automático");
        btnResetar = new JButton("Resetar sistema");

        btnSimular.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simularCiclo();
            }
        });

        btnAuto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alternarModoAutomatico();
            }
        });

        btnResetar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetarSistema();
            }
        });

        painelCampos.add(chkViaA);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 5)));
        painelCampos.add(chkViaB);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 5)));
        painelCampos.add(chkPedestre);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 5)));
        painelCampos.add(chkEmergencia);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblPrioridade = new JLabel("Prioridade das vias:");
        painelCampos.add(lblPrioridade);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 5)));
        painelCampos.add(cmbPrioridade);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 10)));
        painelCampos.add(chkModoNoturno);

        painelCampos.add(Box.createRigidArea(new Dimension(0, 15)));
        painelCampos.add(btnSimular);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 8)));
        painelCampos.add(btnAuto);
        painelCampos.add(Box.createRigidArea(new Dimension(0, 8)));
        painelCampos.add(btnResetar);

        txtHistorico = new JTextArea(14, 24);
        txtHistorico.setEditable(false);
        txtHistorico.setLineWrap(true);
        txtHistorico.setWrapStyleWord(true);

        scrollHistorico = new JScrollPane(txtHistorico);

        painelHistorico = new JPanel(new BorderLayout());
        painelHistorico.add(scrollHistorico, BorderLayout.CENTER);

        painelControle.add(painelCampos, BorderLayout.NORTH);
        painelControle.add(painelHistorico, BorderLayout.CENTER);

        return painelControle;
    }

    private void simularCiclo() {
        if (animando) {
            return;
        }

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

        timerEtapa = new Timer(duracao * 1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();
                animarSequencia(sequencia, indice + 1);
            }
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

        timerContagem = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                segundosRestantes--;

                if (segundosRestantes > 0) {
                    lblContagem.setText("Contagem: " + segundosRestantes + " s");
                } else {
                    lblContagem.setText("Contagem: 0 s");
                    ((Timer) e.getSource()).stop();
                }
            }
        });

        timerContagem.start();
    }

    private void agendarProximoCicloAutomatico() {
        if (!modoAutomatico) {
            btnSimular.setEnabled(true);
            return;
        }

        timerProximoCiclo = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();
                simularCiclo();
            }
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

        modoAutomatico = false;
        animando = false;

        controlador.resetar();
        estadoAtual = EstadoSemaforo.A_VERDE;

        chkViaA.setSelected(false);
        chkViaB.setSelected(false);
        chkPedestre.setSelected(false);
        chkEmergencia.setSelected(false);
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

    private void atualizarTela() {
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

            case PEDESTRE:
                aVermelho.setBackground(Color.RED);
                bVermelho.setBackground(Color.RED);
                pVerde.setBackground(Color.GREEN);
                break;

            case EMERGENCIA:
                aVermelho.setBackground(Color.RED);
                bVermelho.setBackground(Color.RED);
                pVermelho.setBackground(Color.RED);
                break;
        }

        lblEstado.setText("Estado atual: " + estadoAtual.name());
        lblBinario.setText("Binário: " + estadoAtual.getBinario() + "  [A_V, A_A, A_R, B_V, B_A, B_R, P_V]");
        lblDescricao.setText("Descrição: " + estadoAtual.getDescricao());
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
        return borda;
    }

    private void aplicarTema() {
        boolean noturno = chkModoNoturno != null && chkModoNoturno.isSelected();

        Color fundoJanela;
        Color fundoPainel;
        Color fundoSecundario;
        Color texto;

        if (noturno) {
            fundoJanela = new Color(28, 28, 28);
            fundoPainel = new Color(40, 40, 40);
            fundoSecundario = new Color(55, 55, 55);
            texto = Color.WHITE;
        } else {
            fundoJanela = new Color(245, 245, 245);
            fundoPainel = new Color(238, 238, 238);
            fundoSecundario = Color.WHITE;
            texto = Color.BLACK;
        }

        getContentPane().setBackground(fundoJanela);

        if (painelTitulo != null) painelTitulo.setBackground(fundoJanela);
        if (painelCentro != null) painelCentro.setBackground(fundoJanela);
        if (painelSemaforos != null) painelSemaforos.setBackground(fundoJanela);

        if (painelViaA != null) painelViaA.setBackground(fundoPainel);
        if (painelViaB != null) painelViaB.setBackground(fundoPainel);
        if (painelPedestre != null) painelPedestre.setBackground(fundoPainel);
        if (painelInfo != null) painelInfo.setBackground(fundoPainel);
        if (painelControle != null) painelControle.setBackground(fundoPainel);
        if (painelHistorico != null) painelHistorico.setBackground(fundoPainel);

        if (lblTitulo != null) lblTitulo.setForeground(texto);
        if (lblEstado != null) lblEstado.setForeground(texto);
        if (lblBinario != null) lblBinario.setForeground(texto);
        if (lblDescricao != null) lblDescricao.setForeground(texto);
        if (lblRegra != null) lblRegra.setForeground(texto);
        if (lblContagem != null) lblContagem.setForeground(texto);

        configurarCheckBox(chkViaA, fundoPainel, texto);
        configurarCheckBox(chkViaB, fundoPainel, texto);
        configurarCheckBox(chkPedestre, fundoPainel, texto);
        configurarCheckBox(chkEmergencia, fundoPainel, texto);
        configurarCheckBox(chkModoNoturno, fundoPainel, texto);

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
        }
    }

    private void atualizarBordas() {
        if (painelViaA != null) painelViaA.setBorder(criarBordaTitulo("Via A"));
        if (painelViaB != null) painelViaB.setBorder(criarBordaTitulo("Via B"));
        if (painelPedestre != null) painelPedestre.setBorder(criarBordaTitulo("Pedestre"));
        if (painelInfo != null) painelInfo.setBorder(criarBordaTitulo("Informações do Sistema"));
        if (painelControle != null) painelControle.setBorder(criarBordaTitulo("Controles"));
        if (painelHistorico != null) painelHistorico.setBorder(criarBordaTitulo("Histórico"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main tela = new Main();
                tela.setVisible(true);
            }
        });
    }
}