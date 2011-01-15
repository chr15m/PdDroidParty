package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.view.MotionEvent;
import android.util.Log;

public class Slider extends Widget{
	private FloatBuffer vertexBuffer;
	private float vertices[] = new float[16];
	
	public Slider(float x, float y, float w, float h) {
		vertices[0] = x;
		vertices[1] = y;
		vertices[2] = x + w;
		vertices[3] = y;
		vertices[4] = x + w;
		vertices[5] = y;
		vertices[6] = x + w;
		vertices[7] = y + h;
		vertices[8] = x + w;
		vertices[9] = y + h;
		vertices[10] = x;
		vertices[11] = y + h;
		vertices[12] = x;
		vertices[13] = y + h;
		vertices[14] = x;
		vertices[15] = y;
		Log.e("Slider:", "" + x + " " + y + " " + w + " " + h);
		
		// a float is 4 bytes, therefore we multiply the number if vertices with 4.
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
	}

	/**
	 * This function draws our square on screen.
	 * @param gl
	 */
	public void draw(GL10 gl) {
		// Draw UI elements in black
		gl.glColor4f((float)0.0, (float)0.0, (float)0.0, (float)0.0);
		// Enabled the vertices buffer for writing and to be used during rendering.
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		// Specifies the location and data format of an array of vertex
		// coordinates to use when rendering.
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
		//gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);
		gl.glDrawArrays(GL10.GL_LINES, 0, vertices.length);
		// Disable the vertices buffer.
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
}


