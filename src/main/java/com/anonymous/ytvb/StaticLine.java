package com.anonymous.ytvb;

import java.io.PrintStream;

/**
 * Provides a print system that allows you to write to the bottom line of a console while still
 * printing lines. This is useful for console downloading, general progress monitoring, installations, etc.
 */
public class StaticLine {

    public static final String LOG_FORMAT = "[YTViewBot/%s] %s";

    private PrintStream out; // stream to print with
    private String line;     // line to keep static
    private boolean carry;   // true when system has line to carry
    private long carryID;    // id to prevent threads from continuing to use setline after prgm has moved on (as apposed to sleeping until thread exits)

    /**
     * Master constructor for {@link StaticLine}
     * @param out stream object in which to print
     * @param line starting value of the carrying line
     * @param carry whether or not to start the system carrying the line provided in the "line" param
     */
    public StaticLine(PrintStream out, String line, boolean carry) {
        this.line = line;
        this.carry = carry;
        this.out = out;
        this.carryID = System.nanoTime();
        if (carry) enableCarry(line);
    }

    /**
     * Same as {@link StaticLine#StaticLine(PrintStream, String, boolean)} with the "carry" param = true
     * @param out stream object in which to print
     * @param line starting value of the carrying line
     */
    public StaticLine(PrintStream out, String line) {
        this(out, line, true);
    }

    /**
     * Same as {@link StaticLine#StaticLine(PrintStream, String)} with the "out" param = {@link System#out}
     * @param line starting value of the carrying line
     */
    public StaticLine(String line) {
        this(System.out, line);
    }

    /**
     * This is an ID used to ensure that the thread which enabled a particular setLine instance is the only thread with the ability to set text to the carrying line.
     * e.g. Allows you to exit a rewriting thread without the thread accidentally printing another section of it's animation.
     * @return The current systems carry id.
     */
    public long getCarryID() {
        return carryID;
    }

    /**
     * Sets the current line the system is carrying (does nothing if carry = false)
     * @param line Text to set to carrying line
     * @param id The current carry id of the system as provided by {@link StaticLine#getCarryID()}
     * @return whether or not the current carry line as set to the provided value
     */
    public synchronized boolean setLine(String line, long id) {
        if (carry) {
            if (carryID == id) {
                this.line = line;
                out.print("\r");
                out.print(this.line);
            } else return false;
        }
        return carry;
    }

    /**
     * Same as {@link StaticLine#setLine(String, long)} however always returns true unless carry = false
     * @param line Text to set to carrying line
     * @return whether or not the current carry line as set to the provided value
     */
    public synchronized boolean setLine(String line) {
        return setLine(line, getCarryID());
    }

    /**
     * Enables/Disables whether or not to carry the line with println's provided by {@link StaticLine#setLine(String, long)}
     * This allows a prgm to switch between special console rewriting and normal prgm println operations.
     * @param carry Whether or not to carry the rewrite line
     * @param msg if carry = true, is line to carry as set by {@link StaticLine#setLine(String, long)}. If carry = false, is the text to set to current carrying line (as this line cannot be removed)
     */
    public synchronized void setCarry(boolean carry, String msg) {
        if (this.carry == carry) throw new IllegalStateException("Attempted to set carry state to current state");
        this.carry = carry;
        if (carry) enableCarry(msg); else disableCarry(msg);
    }

    /**
     * Standard println function as provided in {@link PrintStream#println(String)}
     * @param m object to print
     */
    public synchronized void println(String m) {
        if (carry) {
            writew_space(m);
            out.print(this.line);
        } else out.println(m);
    }

    private synchronized void log(String message, String replace) { // e.g. [05:05:33] [YTViewBot/INFO] Running.
        println(String.format(LOG_FORMAT, replace, message));
    }

    public synchronized void info(String message) {
        log(message, "INFO");
    }

    public synchronized void warning(String message) {
        log(message, "WARN");
    }

    public synchronized void severe(String message) {
        log(message, "SEVERE");
    }

    private void enableCarry(String msg) {
        this.line = msg;
        out.print(this.line);
    }

    private void disableCarry(String stopMsg) {
        writew_space(stopMsg);
        this.carryID = System.nanoTime();
    }

    private void writew_space(String m) {
        out.print("\r");
        int additionalLength = this.line.length() - m.length();
        StringBuilder sb = new StringBuilder(m);
        for (int i = 0;i<additionalLength;i++) sb.append(" ");
        out.println(sb.toString());
    }
}
