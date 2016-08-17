package com.le.xiaxl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.le.xiaxl.gl_util.GLTextureUtil;
import com.le.xiaxl.gl_util.MatrixState;
import com.le.xiaxl.gl_widget.CameraTextureRect;
import com.le.xiaxl.util.CameraHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MainGLSurfaceView extends GLSurfaceView {
    // 场景渲染器
    private SceneRenderer mRenderer;

    // 渐变矩形的纹理id
    private int mTextureId = 100;
    // 标记 摄像头数据可用
    private boolean isCameraFrameAvailable = false;
    // 纹理渐变的矩形
    private CameraTextureRect mCameraTextureRect;
    // 用于与Camera绑定的 SurfaceTexture
    private SurfaceTexture mSurfaceTexture = null;
    // camera
    private CameraHelper mCameraHelper = CameraHelper.getInstance();

    public MainGLSurfaceView(Context context) {
        super(context);

        // 设置使用OPENGL ES2.0
        this.setEGLContextClientVersion(2);

        // 创建场景渲染器
        mRenderer = new SceneRenderer();
        // 设置渲染器
        setRenderer(mRenderer);

        // 设置渲染模式为主动渲染
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }


    public MainGLSurfaceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        // 设置使用OPENGL ES2.0
        this.setEGLContextClientVersion(2);

        // 创建场景渲染器
        mRenderer = new SceneRenderer();
        // 设置渲染器
        setRenderer(mRenderer);

        // 设置渲染模式为主动渲染
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }


    @Override
    public void onResume() {
        super.onResume();

        if (mSurfaceTexture != null) {
            // 开启摄像机预览
            mCameraHelper.startPreview(Camera.CameraInfo.CAMERA_FACING_BACK, mSurfaceTexture);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止预览
        mCameraHelper.stopPreview();
    }

    /**
     * 销毁
     */
    public void onDestroy() {
        // 停止预览
        mCameraHelper.stopPreview();
    }


    private class SceneRenderer implements Renderer {
        public void onDrawFrame(GL10 gl) {
            //------------取camera数据begin------------
            // 如果camera数据可用，手动取一次
            if (isCameraFrameAvailable) {
                try {
                    if (mSurfaceTexture != null) {
                        mSurfaceTexture.updateTexImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 等待下次可用
                isCameraFrameAvailable = false;
            }
            //-----------取camera数据end-------------

            // 清除深度缓冲与颜色缓冲
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                    | GLES20.GL_COLOR_BUFFER_BIT);

            // 进行渐变矩形的绘制
            MatrixState.pushMatrix();
            MatrixState.translate(0, 0, -1);
            mCameraTextureRect.drawSelf(mTextureId);
            MatrixState.popMatrix();

        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // 设置视窗大小及位置
            GLES20.glViewport(0, 0, width, height);
            // 计算GLSurfaceView的宽高比
            float ratio = (float) width / height;
            // 设置camera位置
            MatrixState.setCamera(
                    //
                    0, // 人眼位置的X
                    0, // 人眼位置的Y
                    1, // 人眼位置的Z
                    //
                    0, // 人眼球看的点X
                    0, // 人眼球看的点Y
                    0, // 人眼球看的点Z
                    // up向量
                    0,
                    1,
                    0);
            // 调用此方法计算产生透视投影矩阵
            MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 1, 20);

        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // 清屏颜色为白色
            GLES20.glClearColor(1, 1, 1, 1);
            // 初始化变换矩阵
            MatrixState.setInitStack();
            //

            // 生成纹理Id
            mTextureId = GLTextureUtil.createOESTextureID();
            // 通过纹理Id，创建SurfaceTexture(该mSurfaceTexture与Camera进行绑定)
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    isCameraFrameAvailable = true;
                }
            });
            // 开启摄像机预览
            mCameraHelper.startPreview(Camera.CameraInfo.CAMERA_FACING_BACK, mSurfaceTexture);
            // 纹理正方形
            mCameraTextureRect = new CameraTextureRect(MainGLSurfaceView.this, 4.0f, 4.0f, mCameraHelper.getPreviewWidth(), mCameraHelper.getPreviewHeight());
        }
    }


}
