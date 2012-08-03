package net.djmacgyver.bgt.map;

import android.graphics.ColorMatrix;
import android.util.FloatMath;

public class HSVManipulationMatrix extends ColorMatrix {
	public HSVManipulationMatrix(float hue/*, float saturation*/, float value) {
		super();
		float twist = (hue % 360f) / 180f * (float) Math.PI;
	    if (twist == 0 && value == 1) return;

	    float cosVal = FloatMath.cos(twist);
	    float sinVal = FloatMath.sin(twist);
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
	            (lumR + cosVal * (1 - lumR) + sinVal * (-lumR))    * value, (lumG + cosVal * (-lumG) + sinVal * (-lumG))     * value, (lumB + cosVal * (-lumB) + sinVal * (1 - lumB)) * value, 0, 0, 
	            (lumR + cosVal * (-lumR) + sinVal * (0.143f))      * value, (lumG + cosVal * (1 - lumG) + sinVal * (0.140f)) * value, (lumB + cosVal * (-lumB) + sinVal * (-0.283f))  * value, 0, 0,
	            (lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR))) * value, (lumG + cosVal * (-lumG) + sinVal * (lumG))      * value, (lumB + cosVal * (1 - lumB) + sinVal * (lumB))  * value, 0, 0, 
	            0f, 0f, 0f, 1f, 0f, 
	            0f, 0f, 0f, 0f, 1f };
		set(mat);
	}
}