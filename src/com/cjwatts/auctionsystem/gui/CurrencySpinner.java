package com.cjwatts.auctionsystem.gui;

import java.text.DecimalFormat;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CurrencySpinner extends JSpinner {
    
    private static final long serialVersionUID = 1L;
    
    private SpinnerNumberModel model;
    private DecimalFormat format;
    
    public CurrencySpinner() {
        super();
        // Model setup
        model = new SpinnerNumberModel(0.0, 0.0, 1000000000.0, 0.01);
        this.setModel(model);
        
        NumberEditor editor = (NumberEditor) this.getEditor();  
        format = editor.getFormat();
        format.setMinimumFractionDigits(2); 
        format.setMaximumFractionDigits(2);
        
        // Step recalculation
        this.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
            	// Steps are sensitive to the current value
                Double magnitude = Math.log10(getDouble());
                if (magnitude < 0) magnitude = 0.0;
                magnitude = Math.ceil(magnitude);
                model.setStepSize(0.01 * Math.pow(10, magnitude));
            }
        });
        
        this.setValue(0.00);
    }
    
    /**
     * Returns the current value as a Double
     */
    public Double getDouble() {
        return (Double) getValue();
    }
    
}
