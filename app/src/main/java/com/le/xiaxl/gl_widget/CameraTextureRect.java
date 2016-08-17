package com.le.xiaxl.gl_widget;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.le.xiaxl.MainGLSurfaceView;
import com.le.xiaxl.gl_util.MatrixState;
import com.le.xiaxl.gl_util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class CameraTextureRect {
    int mProgram;//自定义渲染管线着色器程序id
    int muMVPMatrixHandle;//总变换矩阵引用
    int maPositionHandle; //顶点位置属性引用   
    int maTexCoorHandle; //顶点纹理坐标属性引用 
    int mTextureIdHandle;

    String mVertexShader;//顶点着色器    	 
    String mFragmentShader;//片元着色器

    private FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
    FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲
    int vCount;//顶点数量

    float width;
    float height;
    //按钮右下角的s、t值
    private float mCameraPreviewWidth;
    private float mCameraPreviewHeight;

    public CameraTextureRect(MainGLSurfaceView mv,
                             float width, float height,    //矩形的宽高
                             float cameraPreviewWidth, float cameraPreviewHeight //右下角的s、t值
    ) {

        this.width = width;
        this.height = height;
        this.mCameraPreviewWidth = cameraPreviewWidth;
        this.mCameraPreviewHeight = cameraPreviewHeight;
        initVertexData();
        initShader(mv);

    }

    //初始化顶点坐标与着色数据的方法
    public void initVertexData() {
        //顶点个数
        vCount = 6;
        float vertices[] =
                {
                        -width / 2.0f, height / 2.0f, 0,
                        -width / 2.0f, -height / 2.0f, 0,
                        width / 2.0f, height / 2.0f, 0,

                        -width / 2.0f, -height / 2.0f, 0,
                        width / 2.0f, -height / 2.0f, 0,
                        width / 2.0f, height / 2.0f, 0,
                };
        //创建顶点坐标数据缓冲
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
        //-----------------------注------------------
        // 1、纹理剪裁
        // 我们的需求是正方形图像，这里的纹理图像为1920*1080,所以，我们剪裁掉左右两个部分
        float tempX1 = 0;
        float tempX2 = 1;
        if (mCameraPreviewWidth != 0 && mCameraPreviewHeight != 0) {
            tempX1 = (mCameraPreviewWidth - mCameraPreviewHeight) / (mCameraPreviewWidth * 2f);
            tempX2 = 1 - tempX1;
        }
        //
        float texCoor[] = new float[]//纹理坐标
                {
                        tempX1, 0,
                        tempX1, 1,
                        tempX2, 0,
                        tempX1, 1,
                        tempX2, 1,
                        tempX2, 0
                };
        //创建顶点纹理坐标数据缓冲
        ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asFloatBuffer();
        mTexCoorBuffer.put(texCoor);
        mTexCoorBuffer.position(0);
    }

    public void initShader(MainGLSurfaceView mv) {
        //加载顶点着色器的脚本内容
        mVertexShader = ShaderUtil.loadFromAssetsFile("vertex_tex.sh", mv.getResources());
        //加载片元着色器的脚本内容
        mFragmentShader = ShaderUtil.loadFromAssetsFile("frag_tex.sh", mv.getResources());
        //基于顶点着色器与片元着色器创建程序
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取程序中顶点位置属性引用 
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中总变换矩阵id
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //
        mTextureIdHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
        //获取程序中顶点纹理坐标属性引用
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
    }

    public void drawSelf(int texId) {
        //--------------注--------------
        // 2 、 纹理旋转
        // 此处为顺时针方向旋转90，原因呢，是camera获取的数据不做手动旋转的情况下，是逆时针旋转了90度的
        // 旋转
        MatrixState.rotate(-90, 0, 0, 1);

        //制定使用某套着色器程序
        GLES20.glUseProgram(mProgram);
        //将最终变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        // 将顶点纹理坐标数据传入渲染管线
        GLES20.glVertexAttribPointer
                (
                        maTexCoorHandle,
                        2,
                        GLES20.GL_FLOAT,
                        false,
                        2 * 4,
                        mTexCoorBuffer
                );
        // 启用顶点位置数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);
        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mTextureIdHandle, 0);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId);

        //将顶点法向量数据传入渲染管线
        GLES20.glVertexAttribPointer
                (
                        maPositionHandle,
                        3,
                        GLES20.GL_FLOAT,
                        false,
                        3 * 4,
                        mVertexBuffer
                );
        //绘制矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(maTexCoorHandle);
    }

}
