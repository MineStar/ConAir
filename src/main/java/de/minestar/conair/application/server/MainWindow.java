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

import de.minestar.conair.network.server.DedicatedTCPServer;

public class MainWindow extends JFrame {

    private static MainWindow INSTANCE;

    private static final long serialVersionUID = -7283677754732177328L;

    private JButton _startServer, _stopServer;
    private JTextArea _textArea;
    private JScrollPane _scrollPane;
    private boolean _serverRunning = false;
    private DedicatedTCPServer _server;

    public MainWindow() {
        MainWindow.INSTANCE = this;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel contentPanel = new JPanel();
        setContentPane(contentPanel);

        // set layout
        setLayout(null);

        // set Windowname
        setTitle("ConAir - Server");

        // create GUI
        createGUI();

        // update
        pack();
        setVisible(true);

        setSize(435, 290);

        // Get the size of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = getSize().width;
        int h = getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        setLocation(x, y);

        addComponentListener(new WindowComponentListener());
        resizeComponents();

        // redirect SYSO to TextArea
        MessageConsole mc = new MessageConsole(_textArea);
        mc.redirectOut();
        mc.redirectErr(Color.RED, null);
        mc.setMessageLines(1000);

        System.out.println("Welcome to ConAir!");
    }

    private void resizeComponents() {
        // START SERVER
        int buttonSize = (int) ((getSize().getWidth() - 45) / 2);

        _startServer.setSize(buttonSize, 25);
        _startServer.setLocation(10, 10);

        // STOP SERVER
        _stopServer.setSize(buttonSize, 25);
        _stopServer.setLocation(10 + buttonSize + 10, 10);

        // TEXTAREA
        _scrollPane.setSize((int) getSize().getWidth() - 35, (int) getSize().getHeight() - 90);

        _scrollPane.revalidate();
        revalidate();
    }

    private void createGUI() {
        // START SERVER
        _startServer = new JButton("Start Server");
        _startServer.setSize(200, 25);
        _startServer.setLocation(10, 10);
        _startServer.setEnabled(true);
        _startServer.addActionListener(new StartButtonListener());
        getContentPane().add(_startServer);

        // STOP SERVER
        _stopServer = new JButton("Stop Server");
        _stopServer.setSize(200, 25);
        _stopServer.setLocation(220, 10);
        _stopServer.setEnabled(false);
        _stopServer.addActionListener(new StopButtonListener());
        getContentPane().add(_stopServer);

        // TEXTAREA
        _textArea = new JTextArea();
        _textArea.setEditable(false);
        _scrollPane = new JScrollPane(_textArea);
        _scrollPane.setSize(410, 205);
        _scrollPane.setLocation(10, 45);
        getContentPane().add(_scrollPane);
    }

    private void createServer(int port) {
        try {
            if (_server == null) {
                MainWindow.INSTANCE._startServer.setEnabled(false);
                MainWindow.INSTANCE._stopServer.setEnabled(true);
                MainWindow.INSTANCE._serverRunning = !MainWindow.INSTANCE._serverRunning;
                _server = new DedicatedTCPServer(port, new ArrayList<String>());
            } else {
                System.out.println("ERROR : Server is already running!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            destroyServer();
        }

    }
    private void destroyServer() {
        MainWindow.INSTANCE._stopServer.setEnabled(false);
        MainWindow.INSTANCE._startServer.setEnabled(true);
        MainWindow.INSTANCE._serverRunning = !MainWindow.INSTANCE._serverRunning;
        if (_server != null) {
            _server.stop();
            _server = null;
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
