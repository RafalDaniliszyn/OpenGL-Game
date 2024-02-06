package org.game.renderer;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public class Camera {

    public static Vector3f cameraPosition = new Vector3f(0.0f, 0.0f, 0.0f);

    private static Vector3f cameraRotation = new Vector3f(40.0f, 180.0f, 0.0f);

    private static Matrix4f viewMatrix;
    public static float distance = 15.0f;


    public static Matrix4f getView() {
        if (viewMatrix == null) {
            viewMatrix = new Matrix4f();
        }
        Vector3f cameraPos = cameraPosition;
        Vector3f rotation = cameraRotation;

        viewMatrix.identity();

        viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float)Math.toRadians(-rotation.y), new Vector3f(0, 1, 0));


        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public static void movePosition(float offsetX, float offsetY, float offsetZ) {
        if ( offsetZ != 0 ) {
            cameraPosition.x += (float)Math.sin(Math.toRadians(cameraRotation.y)) * -1.0f * offsetZ;
            cameraPosition.z += (float)Math.cos(Math.toRadians(cameraRotation.y)) * offsetZ;
        }
        if ( offsetX != 0) {
            cameraPosition.x += (float)Math.sin(Math.toRadians(cameraRotation.y - 90)) * -1.0f * offsetX;
            cameraPosition.z += (float)Math.cos(Math.toRadians(cameraRotation.y - 90)) * offsetX;
        }
        cameraPosition.y += offsetY;
    }

    public static void moveRotation(float offsetX, float offsetY, float offsetZ) {
        cameraRotation.x += offsetX;
        cameraRotation.y += offsetY;
        cameraRotation.z += offsetZ;
    }

    public static void setCamera(ShaderProgram shaderProgram) {
        int uViewID = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "uView");
        Matrix4f uView = Camera.getView();
        FloatBuffer viewMatrix = BufferUtils.createFloatBuffer(16);
        uView.get(viewMatrix);
        glUniformMatrix4fv(uViewID, false, viewMatrix);
    }

    public static Vector3f getCameraPosition() {
        return cameraPosition;
    }

    public static void setCameraPosition(Vector3f cameraPosition) {
        Camera.cameraPosition = cameraPosition;
    }

    public static Vector3f getCameraRotation() {
        return cameraRotation;
    }

    public static void setCameraRotation(Vector3f cameraRotation) {
        Camera.cameraRotation = cameraRotation;
    }
}