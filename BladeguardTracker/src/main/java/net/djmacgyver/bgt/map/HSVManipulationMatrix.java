package net.djmacgyver.bgt.map;

import android.graphics.ColorMatrix;
import android.util.FloatMath;

public class HSVManipulationMatrix extends ColorMatrix {
	public HSVManipulationMatrix(float h, float s, float v) {
		// for all that math see...
		// http://beesbuzz.biz/code/hsv_color_transforms.php
		super();
		float twist = (h % 360f) / 180f * (float) Math.PI;

	    float u = FloatMath.cos(twist);
	    float w = FloatMath.sin(twist);
	    /*
	    float lR = 0.213f;
	    float lG = 0.715f;
	    float lB = 0.072f;
	    float[] mat = new float[]
	    { 
	            (lR + u * (1 - lR) + w * (-lR))    * v, (lG + u * (-lG) + w * (-lG))       * v, (lB + u * (-lB) + w * (1 - lB))  * v, 0, 0, 
	            (lR + u * (-lR) + w * (0.143f))    * v, (lG + u * (1 - lG) + w * (0.140f)) * v, (lB + u * (-lB) + w * (-0.283f)) * v, 0, 0,
	            (lR + u * (-lR) + w * (-(1 - lR))) * v, (lG + u * (-lG) + w * (lG))        * v, (lB + u * (1 - lB) + w * (lB))   * v, 0, 0, 
	            0f, 0f, 0f, 1f, 0f, 
	            0f, 0f, 0f, 0f, 1f };
	    */
	    float[] mat = new float[]
        {
	    	.299f * v + .701f * v * s * u + .168f * v * s * w, .587f * v - .587f * v * s * u + .330f * v * s * w, .114f * v - .114f * v * s * u - .497f * v * s * w, 0, 0,
	    	.299f * v - .299f * v * s * u - .328f * v * s * w, .587f * v + .413f * v * s * u + .035f * v * s * w, .114f * v - .114f * v * s * u + .292f * v * s * w, 0, 0,
	    	.299f * v - .3f   * v * s * u + 1.25f * v * s * w, .587f * v - .588f * v * s * u - 1.05f * v * s * w, .114f * v + .886f * v * s * u + .203f * v * s * w, 0, 0,
	    	0, 0, 0, 1, 0,
	    	0, 0, 0, 0, 1
        };
		set(mat);
	}
}