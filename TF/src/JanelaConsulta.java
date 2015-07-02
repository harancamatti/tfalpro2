/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

/**
 *
 * @author Sandro
 */
public class JanelaConsulta extends javax.swing.JFrame {

    private GerenciadorMapa gerenciador;
    private EventosMouse mouse;
    
    private JPanel painelMapa;
    private JPanel painelLateral;
    private DefaultListModel paradasDist;
    private DefaultListModel paradasCrime;
    private JTextField nomeRua;
    private JComboBox faixaHorario;
    private JComboBox diaSemana;
    /**
     * Creates new form JanelaConsulta
     */
    public JanelaConsulta() {
    	super();    	
        //initComponents();

        GeoPosition poa = new GeoPosition(-30.05, -51.18);
        gerenciador = new GerenciadorMapa(poa, GerenciadorMapa.FonteImagens.VirtualEarth);
        mouse = new EventosMouse();        		
        gerenciador.getMapKit().getMainMap().addMouseListener(mouse);
        gerenciador.getMapKit().getMainMap().addMouseMotionListener(mouse);       

        painelMapa = new JPanel();
        painelMapa.setLayout(new BorderLayout());
        painelMapa.add(gerenciador.getMapKit(), BorderLayout.CENTER);
                
        getContentPane().add(painelMapa, BorderLayout.CENTER);
        
        painelLateral = new JPanel(new GridLayout(3,1));
        getContentPane().add(painelLateral, BorderLayout.WEST);
        
        JPanel painelDist = new JPanel(new BorderLayout());
        JLabel jLabel1 = new javax.swing.JLabel();
        jLabel1.setText("Por Distancia");
        painelDist.add(jLabel1, BorderLayout.NORTH);
        JScrollPane jScrollPane = new javax.swing.JScrollPane();
        JList jList1 = new javax.swing.JList();
        paradasDist = new DefaultListModel();
        jList1.setModel(paradasDist);
        jScrollPane.setViewportView(jList1);
        painelDist.add(jScrollPane, BorderLayout.CENTER);
        painelLateral.add(painelDist);

        JPanel painelCrime = new JPanel(new BorderLayout());
        JLabel jLabel2 = new javax.swing.JLabel();
        jLabel2.setText("Por Criminalidade");
        painelCrime.add(jLabel2, BorderLayout.NORTH);
        JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        JList jList2 = new javax.swing.JList();
        paradasCrime = new DefaultListModel();
        jList2.setModel(paradasCrime);
        jScrollPane2.setViewportView(jList2);
        painelCrime.add(jScrollPane2, BorderLayout.CENTER);
        painelLateral.add(painelCrime);
        
        JPanel footer = new JPanel(new GridLayout(4,1));
        
        nomeRua = new JTextField();
        footer.add(nomeRua);

        faixaHorario = new JComboBox(new String[] { "TODOS", "00:01 às 06:00", "06:01 às 12:00", "12:01 às 18:00", "18:01 às 24:00" });
        footer.add(faixaHorario);

        diaSemana = new JComboBox(new String[] { "TODOS", "segunda-feira", "terça-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sábado", "domingo"});
        footer.add(diaSemana);
        
        JButton btnNewButton = new JButton("Consulta");
        footer.add(btnNewButton);
        
        painelLateral.add(footer);
                
        btnNewButton.addActionListener(new ActionListener() {
                @Override
        	public void actionPerformed(ActionEvent e) {
        		consulta(e);
        	}
        });
        
        this.setSize(800,600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    }

    private int calcDist2d(GeoPosition a, GeoPosition b){
        Point2D point = gerenciador.getMapKit().getMainMap().convertGeoPositionToPoint(a);
        Point2D pont2 = gerenciador.getMapKit().getMainMap().convertGeoPositionToPoint(b);
        return (int) Math.sqrt(Math.pow(point.getX()-pont2.getX(),2)+
                        Math.pow(point.getY()-pont2.getY(),2));
        
    }
    
    private void consulta(java.awt.event.ActionEvent evt) {

        // Para obter o centro e o raio, usar como segue:
    	GeoPosition centro = gerenciador.getSelecaoCentro();
    	int raio = gerenciador.getRaio();        

        List<Crime> crimes = Crime.find((String)faixaHorario.getSelectedItem(), (String)diaSemana.getSelectedItem());
            
        // Lista para armazenar o resultado da consulta
        List<MyWaypoint> lstPoints = MyWaypoint.find(nomeRua.getText());
        Stream<MyWaypoint> stream = lstPoints.stream();
        stream = stream.filter(wp -> calcDist2d(centro, wp.getPosition())*2 < calcDist2d(centro, gerenciador.getSelBorda()));
        stream = stream.map(wp -> wp.calcCrime(crimes));
        lstPoints = stream.collect(Collectors.toList());
        
        Collections.sort(lstPoints, Comparator.comparing(item -> calcDist2d(centro, item.getPosition())));
        paradasDist.removeAllElements();
        for (MyWaypoint lstPoint : lstPoints) {
            paradasDist.addElement(lstPoint.toString());
        }

        Collections.sort(lstPoints, Comparator.comparing(item -> item.getValue()));
        paradasCrime.removeAllElements();
        for (MyWaypoint lstPoint : lstPoints) {
            paradasCrime.add(0,lstPoint.toString());
        }

        // Informa o resultado para o gerenciador
        gerenciador.setPontos(lstPoints);
        // Informa o intervalo de valores gerados, para calcular a cor de cada ponto
        double menorValor = lstPoints.stream().min(Comparator.comparing(item -> item.getValue())).get().getValue();
        double maiorValor = lstPoints.stream().max(Comparator.comparing(item -> item.getValue())).get().getValue();
        gerenciador.setIntervaloValores(menorValor, maiorValor);        
        
        this.repaint();
    }
    
    private class EventosMouse extends MouseAdapter
    {
    	private int lastButton = -1;    	
    	
    	@Override
    	public void mousePressed(MouseEvent e) {
    		JXMapViewer mapa = gerenciador.getMapKit().getMainMap();
    		GeoPosition loc = mapa.convertPointToGeoPosition(e.getPoint());
//    		System.out.println(loc.getLatitude()+", "+loc.getLongitude());
    		lastButton = e.getButton();
    		// Botão 3: seleciona localização
    		if(lastButton==MouseEvent.BUTTON3) {  			
    			gerenciador.setSelecaoCentro(loc);
    			gerenciador.setSelecaoBorda(loc);
    			//gerenciador.getMapKit().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    			gerenciador.getMapKit().repaint();    			
    		}
    	}    
    	
            @Override
    	public void mouseDragged(MouseEvent e) {
    		// Arrasta com o botão 3 para definir o raio
    		if(lastButton ==  MouseEvent.BUTTON3) {    			
    			JXMapViewer mapa = gerenciador.getMapKit().getMainMap();
    			gerenciador.setSelecaoBorda(mapa.convertPointToGeoPosition(e.getPoint()));
    			gerenciador.getMapKit().repaint();
    		}    			
    	}
    } 	
}
