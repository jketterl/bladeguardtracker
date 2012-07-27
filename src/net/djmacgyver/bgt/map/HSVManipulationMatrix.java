package net.djmacgyver.bgt.map;

import android.graphics.ColorMatrix;

public class HSVManipulationMatrix extends ColorMatrix {
	public HSVManipulationMatrix(float hue/*, float saturation, float value*/) {
		super();
		float value = (hue % 360f) / 180f * (float) Math.PI;
	    if (value == 0) return;
	    System.out.println("input: " + hue + "; output: " + value);

	    float cosVal = (float) Math.cos(value);
	    float sinVal = (float) Math.sin(value);
	    /*
	    float lumR = 0.33333f;
	    float lumG = 0.33333f;
	    float lumB = 0.33333f;
	    */
	    float lumR = 0.213f;
	    float lumG = 0.715f;
	    float lumB = 0.072f;
	    float[] mat = new float[]
	    { 
	            lumR + cosVal * (1 - lumR) + sinVal * (-lumR),    lumG + cosVal * (-lumG) + sinVal * (-lumG),     lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0, 
	            lumR + cosVal * (-lumR) + sinVal * (0.143f),      lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f),  0, 0,
	            lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG),      lumB + cosVal * (1 - lumB) + sinVal * (lumB),  0, 0, 
	            0f, 0f, 0f, 1f, 0f, 
	            0f, 0f, 0f, 0f, 1f };
		set(mat);
	}
}