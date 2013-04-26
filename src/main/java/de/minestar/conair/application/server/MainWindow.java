/*
 * Copyright (C) 2013 MineStar.de 
 * 
 * This file is part of ConAir.
 * 
 * ConAir is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * ConAir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ConAir.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.conair.application.server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.minestar.conair.network.server.ChatServer;

public class MainWindow extends JFrame {

    private static MainWindow INSTANCE;

    private static final long serialVersionUID = -7283677754732177328L;

    private JButton startServer, stopServer;
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private boolean serverRunning = false;

    private ChatServer server;
    private Thread serverThread;

    public MainWindow() {
        MainWindow.INSTANCE = this;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel contentPanel = new JPanel();
        setContentPane(contentPanel);

        // set layout
        this.setLayout(null);

        // set Windowname
        this.setTitle("ConAir - Server");

        // create GUI
        this.createGUI();

        // update
        pack();
        setVisible(true);

        this.setSize(435, 290);

        // Get the size of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = getSize().width;
        int h = getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        setLocation(x, y);

        this.addComponentListener(new WindowComponentListener());
        this.resizeComponents();

        // redirect SYSO to TextArea
        MessageConsole mc = new MessageConsole(this.textArea);
        mc.redirectOut();
        mc.redirectErr(Color.RED, null);
        mc.setMessageLines(1000);

        System.out.println("Welcome to ConAir!");
    }

    private void resizeComponents() {
        // START SERVER
        int buttonSize = (int) ((this.getSize().getWidth() - 45) / 2);

        startServer.setSize(buttonSize, 25);
        startServer.setLocation(10, 10);

        // STOP SERVER
        stopServer.setSize(buttonSize, 25);
        stopServer.setLocation(10 + buttonSize + 10, 10);

        // TEXTAREA
        scrollPane.setSize((int) this.getSize().getWidth() - 35, (int) this.getSize().getHeight() - 90);

        scrollPane.revalidate();
        revalidate();
    }

    private void createGUI() {
        // START SERVER
        startServer = new JButton("Start Server");
        startServer.setSize(200, 25);
        startServer.setLocation(10, 10);
        startServer.setEnabled(true);
        startServer.addActionListener(new StartButtonListener());
        this.getContentPane().add(startServer);

        // STOP SERVER
        stopServer = new JButton("Stop Server");
        stopServer.setSize(200, 25);
        stopServer.setLocation(220, 10);
        stopServer.setEnabled(false);
        stopServer.addActionListener(new StopButtonListener());
        this.getContentPane().add(stopServer);

        // TEXTAREA
        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setSize(410, 205);
        scrollPane.setLocation(10, 45);
        this.getContentPane().add(scrollPane);
    }

    private void createServer(int port) {
        try {
            if (this.server == null) {
                MainWindow.INSTANCE.startServer.setEnabled(false);
                MainWindow.INSTANCE.stopServer.setEnabled(true);
                MainWindow.INSTANCE.serverRunning = !MainWindow.INSTANCE.serverRunning;
                server = new ChatServer(port, new ArrayList<String>());
                serverThread = new Thread(server);
                serverThread.start();

//                Thread.sleep(500);
//                ChatClient client = new ChatClient(new MainPacketHandler(), "localhost", 9002);
//                Thread cThread = new Thread(client);
//
//                PacketType.registerPacket(HelloWorldPacket.class);
//
//                cThread.start();
//
//                NetworkPacket packet = new HelloWorldPacket("Hallo Welt!");
//                for (int i = 0; i < 300; i++) {
//                    client.sendPacket(packet);
//                }
//
//                Thread.sleep(2000);
//                client.stop();
//                cThread.stop();
            } else {
                System.out.println("ERROR : Server is already running!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            destroyServer();
        }

    }

    @SuppressWarnings("deprecation")
    private void destroyServer() {
        try {
            MainWindow.INSTANCE.stopServer.setEnabled(false);
            MainWindow.INSTANCE.startServer.setEnabled(true);
            MainWindow.INSTANCE.serverRunning = !MainWindow.INSTANCE.serverRunning;
            if (this.server != null) {
                this.server.stop();
                this.server = null;
                if (this.serverThread != null) {
                    this.serverThread.stop();
                    this.serverThread = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // /////////////////////////////////////////////////////////////////////
    //
    // STATIC
    //
    // /////////////////////////////////////////////////////////////////////

    public static void stopServer() {
        MainWindow.INSTANCE.destroyServer();
    }

    public static void resizeGUI() {
        MainWindow.INSTANCE.resizeComponents();
    }

    public static void startServer() {
        int port = 9000;

        boolean inputCorrect = false;
        while (!inputCorrect) {
            String input = JOptionPane.showInputDialog(null, "Enter port: ", "Enter port please...", 1);
            if (input == null) {
                port = -1;
                break;
            }
            input = input.replace(" ", "");
            try {
                port = Integer.valueOf(input);
                inputCorrect = true;
            } catch (Exception e) {
                inputCorrect = false;
            }
        }
        if (port > 0) {
            MainWindow.INSTANCE.createServer(port);
        } else {
            System.out.println("Cancelled serverstart!");
        }
    }
    // /////////////////////////////////////////////////////////////////////
    //
    // LISTENERS
    //
    // /////////////////////////////////////////////////////////////////////

    class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            MainWindow.startServer();
        }
    }

    class StopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            MainWindow.stopServer();
        }
    }

    class WindowComponentListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            MainWindow.resizeGUI();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

    public static void main(String[] args) {
        new MainWindow();
    }

}
