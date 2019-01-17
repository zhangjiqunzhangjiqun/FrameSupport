package com.frame.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.frame.R;

import java.io.File;
import java.math.BigDecimal;

/**
 * 图片处理类
 */
public class GlideImageUtil {

    /**
     * 显示图片
     */
    public static void showImage(Context context, String url, ImageView view) {
        Glide.with(context).load(url)//图片地址
                .thumbnail(0.1f)//先加载缩略图 然后在加载全图
                .apply(new RequestOptions().placeholder(R.drawable.img_showing)
                        .error(R.drawable.img_show_error))
                //  .override(600,600)//指定尺寸(图片大小在xml中不能写死,是wrap_content才可以指定尺寸)
                //    .crossFade(300)//加载动画(默认300)
                .into(view);
    }

    /**
     * 圆形头像
     */
    public static void showCircularImage(final Context context, String url, ImageView view) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .thumbnail(0.1f)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.img_showing)
                        .error(R.drawable.img_show_error))
                //   .load( Uri.fromFile( new File( filePath ) ))  //(显示本地视频)
                //   .asGif()//显示gif动画,asGif()判断是否是gif动画
                // .skipMemoryCache()//跳过内存缓存
                // .diskCacheStrategy(DiskCacheStrategy.NONE)//跳过硬盘缓存(什么都不缓存)
                //    .diskCacheStrategy(DiskCacheStrategy.SOURCE)仅仅只缓存原来的全分辨率的图像
                //     .diskCacheStrategy(DiskCacheStrategy.RESULT)仅仅缓存最终的图像
                //     .diskCacheStrategy(DiskCacheStrategy.ALL)缓存所有版本的图像（默认行为）
                //.priority(Priority.HIGH)//设置图片加载的顺序
                // .bitmapTransform(new CropCircleTransformation(context))//圆形图
                .into(new BitmapImageViewTarget(view) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        view.setImageDrawable(circularBitmapDrawable);
                    }
                });
        //   .bitmapTransform(new BlurTransformation(context, radius))//实现高斯模糊radius取值1-25,值越大图片越模糊
        //   .bitmapTransform(new BlurTransformation(context, 25), new CropCircleTransformation(context))//原图基础上变换成圆图 +毛玻璃（高斯模糊）
        //   .bitmapTransform(new RoundedCornersTransformation(context, 30, 0, RoundedCornersTransformation.CornerType.BOTTOM))//原图处理成圆角 如果是四周已经是圆角则RoundedCornersTransformation.CornerType.ALL
    }

    /**
     * 加载GIF图片
     */
    public static void showGisImage(Context context, String url, ImageView view) {
        Glide.with(context)
                .asGif()
                .load(url)
                .thumbnail(0.1f)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.img_showing)
                        .error(R.drawable.img_show_error))
                .into(view);
    }

    /**
     * 加载本地资源
     */
    public static void showLocalImage(Context context, boolean isGif, int url, ImageView view) {
        if (isGif) {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.img_showing)
                            .error(R.drawable.img_show_error))
                    .into(view);
        } else {
            Glide.with(context)
                    .load(url)
                    .thumbnail(0.1f)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.img_showing)
                            .error(R.drawable.img_show_error))
                    .into(view);
        }
    }

    /**
     * 取消和恢复请求(一般用于listview高速滑动中)
     * 0:滑动中1:停止滑动
     * 当列表在滑动的时候，调用pauseRequests()取消请求，滑动停止时，调用resumeRequests()恢复请求。
     */
    public static void suspendImage(Context context, int type) {
        if (type == 0) {
            Glide.with(context).pauseRequests();
        } else {
            Glide.with(context).resumeRequests();
        }
    }

    /**
     * 清除图片磁盘缓存
     */
    public static void clearImageDiskCache(final Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(context).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(context).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片内存缓存
     */
    public static void clearImageMemoryCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片所有缓存
     */
    public static void clearImageAllCache(Context context) {
        clearImageDiskCache(context);
        clearImageMemoryCache(context);
        String ImageExternalCatchDir = context.getExternalCacheDir() + ExternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR;
        deleteFolderFile(ImageExternalCatchDir, true);
    }

    /**
     * 获取Glide造成的缓存大小
     */
    public static String getCacheSize(Context context) {
        try {
            return getFormatSize(getFolderSize(new File(context.getCacheDir() + "/" + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     */
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     */
    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        file.delete();
                    } else {
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 格式化单位
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }
        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }
        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }
        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);

        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }


}