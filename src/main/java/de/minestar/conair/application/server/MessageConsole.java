package de.minestar.conair.application.server;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*
 *  Create a simple console to display text messages.
 *
 *  Messages can be directed here from different sources. Each source can
 *  have its messages displayed in a different color.
 *
 *  Messages can either be appended to the console or inserted as the first
 *  line of the console
 *
 *  You can limit the number of lines to hold in the Document.
 */
public class MessageConsole {
    private JTextComponent _textComponent;
    private Document _document;
    private boolean _isAppend;
    private DocumentListener _limitLinesListener;

    public MessageConsole(JTextComponent textComponent) {
        this(textComponent, true);
    }

    /*
     * Use the text component specified as a simply console to display text messages.
     * 
     * The messages can either be appended to the end of the console or inserted as the first line of the console.
     */
    public MessageConsole(JTextComponent textComponent, boolean isAppend) {
        _textComponent = textComponent;
        _document = textComponent.getDocument();
        _isAppend = isAppend;
        textComponent.setEditable(false);
    }

    /*
     * Redirect the output from the standard output to the console using the default text color and null PrintStream
     */
    public void redirectOut() {
        redirectOut(null, null);
    }

    /*
     * Redirect the output from the standard output to the console using the specified color and PrintStream. When a PrintStream is specified the message will be added to the Document before it is also written to the PrintStream.
     */
    public void redirectOut(Color textColor, PrintStream printStream) {
        ConsoleOutputStream cos = new ConsoleOutputStream(textColor, printStream);
        System.setOut(new PrintStream(cos, true));
    }

    /*
     * Redirect the output from the standard error to the console using the default text color and null PrintStream
     */
    public void redirectErr() {
        redirectErr(null, null);
    }

    /*
     * Redirect the output from the standard error to the console using the specified color and PrintStream. When a PrintStream is specified the message will be added to the Document before it is also written to the PrintStream.
     */
    public void redirectErr(Color textColor, PrintStream printStream) {
        ConsoleOutputStream cos = new ConsoleOutputStream(textColor, printStream);
        System.setErr(new PrintStream(cos, true));
    }

    /*
     * To prevent memory from being used up you can control the number of lines to display in the console
     * 
     * This number can be dynamically changed, but the console will only be updated the next time the Document is updated.
     */
    public void setMessageLines(int lines) {
        if (_limitLinesListener != null)
            _document.removeDocumentListener(_limitLinesListener);

        _limitLinesListener = new LimitLinesDocumentListener(lines, _isAppend);
        _document.addDocumentListener(_limitLinesListener);
    }

    /*
     * Class to intercept output from a PrintStream and add it to a Document. The output can optionally be redirected to a different PrintStream. The text displayed in the Document can be color coded to indicate the output source.
     */
    class ConsoleOutputStream extends ByteArrayOutputStream {
        private SimpleAttributeSet _attributes;
        private PrintStream _printStream;
        private StringBuffer _buffer = new StringBuffer(80);
        private boolean _isFirstLine;

        /*
         * Specify the option text color and PrintStream
         */
        public ConsoleOutputStream(Color textColor, PrintStream printStream) {
            if (textColor != null) {
                _attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(_attributes, textColor);
            }

            _printStream = printStream;

            if (_isAppend)
                _isFirstLine = true;
        }

        /*
         * Override this method to intercept the output text. Each line of text output will actually involve invoking this method twice:
         * 
         * a) for the actual text message b) for the newLine string
         * 
         * The message will be treated differently depending on whether the line will be appended or inserted into the Document
         */
        public void flush() {
            String message = toString();

            if (message.length() == 0)
                return;

            if (_isAppend)
                handleAppend(message);
            else
                handleInsert(message);

            reset();
        }

        /*
         * We don't want to have blank lines in the Document. The first line added will simply be the message. For additional lines it will be:
         * 
         * newLine + message
         */
        private void handleAppend(String message) {
            if (message.endsWith("\r") || message.endsWith("\n")) {
                _buffer.append(message);
            } else {
                _buffer.append(message);
                clearBuffer();
            }
        }

        /*
         * We don't want to merge the new message with the existing message so the line will be inserted as:
         * 
         * message + newLine
         */
        private void handleInsert(String message) {
            _buffer.append(message);

            if (message.endsWith("\r") || message.endsWith("\n")) {
                clearBuffer();
            }
        }

        /*
         * The message and the newLine have been added to the buffer in the appropriate order so we can now update the Document and send the text to the optional PrintStream.
         */
        private void clearBuffer() {
            // In case both the standard out and standard err are being
            // redirected
            // we need to insert a newline character for the first line only

            if (_isFirstLine && _document.getLength() != 0) {
                _buffer.insert(0, "\n");
            }

            _isFirstLine = false;
            String line = _buffer.toString();

            try {
                if (_isAppend) {
                    int offset = _document.getLength();
                    _document.insertString(offset, line, _attributes);
                    _textComponent.setCaretPosition(_document.getLength());
                } else {
                    _document.insertString(0, line, _attributes);
                    _textComponent.setCaretPosition(0);
                }
            } catch (BadLocationException ble) {
            }

            if (_printStream != null) {
                _printStream.print(line);
            }

            _buffer.setLength(0);
        }
    }
}
