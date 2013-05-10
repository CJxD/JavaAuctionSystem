package com.cjwatts.auctionsystem.gui;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CurrencySpinner extends JSpinner {
    
    private static final long serialVersionUID = 1L;
    
    private SpinnerNumberModel model;
    
    public CurrencySpinner() {
        super();
        // Model setup
        model = new SpinnerNumberModel(0.0, 0.0, Double.MAX_VALUE, 0.1);
        this.setModel(model);
        
        // Step recalculation
        this.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
            	// Steps are sensitive to the current value
                Double magnitude = Math.log10(getDouble());
                Double stepSize = Math.floor(magnitude) / 2;
                model.setStepSize(Math.pow(10, stepSize));
            }
        });
    }
    
    /**
     * Returns the current value as a Double
     */
    public Double getDouble() {
        return (Double) getValue();
    }
    
}
