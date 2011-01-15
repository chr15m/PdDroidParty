package cx.mccormick.pddroidparty;

// originally from here: http://blog.jayway.com/2009/12/03/opengl-es-tutorial-for-android-part-i/

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;

public class OpenGLRenderer implements Renderer {
	// initialise a test slider
	ArrayList<Widget> widgets;
	
	public OpenGLRenderer(ArrayList<Widget> in) {
		widgets = in;
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		/*// Set the background color to black ( rgba ).
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		// Enable Smooth Shading, default not really needed.
		gl.glShadeModel(GL10.GL_SMOOTH);
		// Depth buffer setup.
		gl.glClearDepthf(1.0f);
		// Enables depth testing.
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// The type of depth testing to do.
		gl.glDepthFunc(GL10.GL_LEQUAL);
		// Really nice perspective calculations.
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);*/
	}

	public void onDrawFrame(GL10 gl) {
		/*// Clears the screen and depth buffer.
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);		
		
		// Translates 4 units into the screen.
		gl.glTranslatef(0, 0, -4); // OpenGL docs
		*/

		// Replace the current matrix with the identity matrix
		gl.glLoadIdentity(); // OpenGL docs
		// trick for exact pixels
		//gl.glTranslatef((float)0.375, (float)0.375, (float)0);
		
		gl.glClearColor((float)1.0, (float)1.0, (float)1.0, (float)1.0);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// draw all widgets
		if (widgets != null) {
			for (Widget widget: widgets) {
				widget.draw(gl);
			}
		}
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0, (float)1.0, (float)1.0, 0, 0, 1);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
	}
}
