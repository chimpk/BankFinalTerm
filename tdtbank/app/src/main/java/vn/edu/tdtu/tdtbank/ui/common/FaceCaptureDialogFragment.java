package vn.edu.tdtu.tdtbank.ui.common;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.DialogFaceBinding;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.ImageUtils;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Chụp ảnh khuôn mặt bằng ứng dụng camera hệ thống và kiểm tra bằng ML Kit (chạy trên thiết bị):
 * đúng 1 khuôn mặt; nếu requireSmile = true thì phải mỉm cười (thử thách "sống" mức cơ bản).
 *
 * GIỚI HẠN: không so khớp khuôn mặt với người đã đăng ký. Việc gắn ảnh với danh tính thật do nhân viên
 * đối chiếu giấy tờ (docs/limitations.md).
 */
public class FaceCaptureDialogFragment extends DialogFragment {

    public static final String KEY_IMAGE = "imageBase64";
    public static final String KEY_FACE_DETECTED = "faceDetected";
    public static final String KEY_LIVENESS = "livenessPassed";
    private static final String ARG_REQUEST = "request";
    private static final String ARG_SMILE = "smile";
    private static final float SMILE_THRESHOLD = 0.6f;

    private DialogFaceBinding binding;
    private ActivityResultLauncher<Uri> takePicture;
    private File photoFile;
    private String imageBase64;
    private boolean faceDetected;
    private boolean livenessPassed;
    private FaceDetector detector;

    public static FaceCaptureDialogFragment newInstance(String requestKey, boolean requireSmile) {
        FaceCaptureDialogFragment f = new FaceCaptureDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_REQUEST, requestKey);
        b.putBoolean(ARG_SMILE, requireSmile);
        f.setArguments(b);
        f.setCancelable(false);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        takePicture = registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
            if (ok != null && ok) {
                analyze();
            }
        });
        detector = FaceDetection.getClient(new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogFaceBinding.inflate(LayoutInflater.from(requireContext()));
        boolean smile = requireArguments().getBoolean(ARG_SMILE);
        binding.tvInstruction.setText(smile ? R.string.face_instruction_smile : R.string.face_instruction);
        binding.btnCapture.setOnClickListener(v -> launchCamera());
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.face_title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.face_use_photo, null)
                .setNegativeButton(R.string.action_cancel, (d, w) -> dismissAllowingStateLoss())
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isAccepted());
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> deliver());
        }
    }

    private boolean isAccepted() {
        boolean smile = requireArguments().getBoolean(ARG_SMILE);
        return imageBase64 != null && faceDetected && (!smile || livenessPassed);
    }

    private void launchCamera() {
        try {
            File dir = new File(requireContext().getCacheDir(), "kyc");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("mkdirs");
            }
            photoFile = new File(dir, "face.jpg");
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", photoFile);
            takePicture.launch(uri);
        } catch (Exception e) {
            binding.tvResult.setText(R.string.face_camera_unavailable);
        }
    }

    private void analyze() {
        if (photoFile == null || binding == null) {
            return;
        }
        try {
            Bitmap bmp = ImageUtils.decodeScaled(requireContext().getContentResolver(),
                    Uri.fromFile(photoFile), Constants.MAX_FACE_IMAGE_EDGE);
            binding.ivPreview.setImageBitmap(bmp);
            imageBase64 = ImageUtils.toBase64Jpeg(bmp, Constants.FACE_JPEG_QUALITY);
            binding.tvResult.setText(R.string.face_analyzing);
            detector.process(InputImage.fromBitmap(bmp, 0))
                    .addOnSuccessListener(this::onFaces)
                    .addOnFailureListener(e -> {
                        faceDetected = false;
                        livenessPassed = false;
                        showResult(R.string.face_detect_failed);
                    });
        } catch (IOException e) {
            showResult(R.string.face_camera_unavailable);
        }
    }

    private void onFaces(List<Face> faces) {
        faceDetected = faces.size() == 1;
        Float smileProb = faceDetected ? faces.get(0).getSmilingProbability() : null;
        livenessPassed = faceDetected && smileProb != null && smileProb >= SMILE_THRESHOLD;
        boolean needSmile = requireArguments().getBoolean(ARG_SMILE);
        if (faces.isEmpty()) {
            showResult(R.string.face_none);
        } else if (faces.size() > 1) {
            showResult(R.string.face_multiple);
        } else if (needSmile && !livenessPassed) {
            showResult(R.string.face_not_smiling);
        } else {
            showResult(R.string.face_ok);
        }
    }

    private void showResult(int res) {
        if (binding == null) {
            return;
        }
        binding.tvResult.setText(res);
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isAccepted());
        }
    }

    private void deliver() {
        Bundle b = new Bundle();
        b.putString(KEY_IMAGE, imageBase64);
        b.putBoolean(KEY_FACE_DETECTED, faceDetected);
        b.putBoolean(KEY_LIVENESS, livenessPassed);
        getParentFragmentManager().setFragmentResult(requireArguments().getString(ARG_REQUEST), b);
        dismissAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (detector != null) {
            detector.close();
        }
        if (photoFile != null && photoFile.exists() && !photoFile.delete()) {
            photoFile.deleteOnExit(); // ảnh tạm trong cache, không để lại trên máy
        }
    }
}
