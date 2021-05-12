package com.lifeshare.imagepicker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.lifeshare.R
import com.lifeshare.customview.CustomProgressDialog
import com.lifeshare.permission.RuntimeEasyPermission
import com.lifeshare.utils.compressImageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class ImagePickerFragment : DialogFragment(), View.OnClickListener, RuntimeEasyPermission.PermissionCallbacks {
    var tvGallery: TextView? = null
    var tvCamera: TextView? = null
    private val permissions_camera = arrayOf(Manifest.permission.CAMERA)
    private var mListener: ImagePickerListener? = null
    var mProgressDialog: CustomProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return if (arguments != null && requireArguments().containsKey(KEY_PICKER_TYPE)) {
            if (requireArguments().getInt(KEY_PICKER_TYPE) == 0) {
                RuntimeEasyPermission.newInstance(permissions_camera,
                        REQUEST_CAPTURE_PERM, "Allow camera permission")
                        .show(childFragmentManager)
                return null
            }
            null
        } else {
            inflater.inflate(R.layout.capture_dialogue, container)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvGallery = view.findViewById<View>(R.id.tv_gallery) as TextView
        tvCamera = view.findViewById<View>(R.id.tv_camera) as TextView
        tvGallery!!.setOnClickListener(this)
        tvCamera!!.setOnClickListener(this)
    }

    fun show(fragmentManager: FragmentManager) {
        //show(fragmentManager, TAG);
        if (fragmentManager.findFragmentByTag(TAG) == null) {
            fragmentManager.beginTransaction().add(this, TAG).commitAllowingStateLoss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ImagePickerListener) {
            mListener = context
        }
        if (parentFragment is ImagePickerListener) {
            mListener = parentFragment as ImagePickerListener?
        }
        if (mListener == null) {
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener!!.onImagePickerClose()
        mListener = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ACTION_CHOOSE_IMAGE -> {
                    var realPathPhotoId: String? = null

                    GlobalScope.launch(Dispatchers.Main) {
                        showLoading()

                        if (data?.data != null) {     //Photo from gallery
                            val imgPath = data!!.data!!.path
                            val imgUri = data.data
                            realPathPhotoId = compressImageFile(imgPath!!, false, imgUri!!)
                            val selectedImageName = MediaHelper.createMediaFileName(requireArguments().getString(KEY_INITIAL_NAME, ""))

                            try {
                                if (realPathPhotoId != null) {
                                    MediaHelper.getInstance().copyFile(realPathPhotoId, requireContext().getExternalFilesDir(null).toString() + "/" + selectedImageName)
                                    mListener!!.onImageSelected(requireArguments().getInt(KEY_PICKER_REQUEST_CODE, 0), requireContext().getExternalFilesDir(null).toString() + "/" + selectedImageName, selectedImageName)
                                } else {
                                    Toast.makeText(requireContext(), R.string.err_image_not_exist_in_device, Toast.LENGTH_SHORT).show()
                                }
                                closeImagePicker()
                            } catch (e: IOException) {
                                closeImagePicker()
                                e.printStackTrace()
                            }
                        }
                        hideLoading()

                    }


                }
                ACTION_CAPTURE_IMAGE -> {
                    var captureImageName = ""
                    captureImageName = MediaHelper.getInstance()
                            .imageCapturePath.substring(MediaHelper.getInstance()
                                    .imageCapturePath.lastIndexOf("/") + 1)
                    MediaHelper.getInstance().singleMediaScanner(requireActivity())
                    try {
                        MediaHelper.getInstance().copyFile(MediaHelper.getInstance()
                                .imageCapturePath, requireContext().getExternalFilesDir(null).toString() + "/" + captureImageName)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    mListener!!.onImageSelected(requireArguments().getInt(KEY_PICKER_REQUEST_CODE, 0), requireContext().getExternalFilesDir(null).toString() + "/" + captureImageName, captureImageName)
                    closeImagePicker()
                }
            }
        } else {
            closeImagePicker()
        }
    }

    private fun closeImagePicker() {
        dismissAllowingStateLoss()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_gallery -> selectImageNew()
            R.id.tv_camera -> RuntimeEasyPermission.newInstance(permissions_camera,
                    REQUEST_CAPTURE_PERM, "Allow camera permission")
                    .show(childFragmentManager)
            else -> {
            }
        }
    }

    private fun selectImageNew() {
        MediaHelper.createMediaDirectory()
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, ACTION_CHOOSE_IMAGE)
    }

    private fun captureImageNew() {
        // Create a media file name
        val mImageName = MediaHelper.createMediaFileName(requireArguments().getString(KEY_INITIAL_NAME, ""))
        val mediaFile: File
        mediaFile = File(requireContext().getExternalFilesDir(null), mImageName)
        MediaHelper.getInstance().imageCapturePath = mediaFile.absolutePath
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", mediaFile))
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile))
        }
        startActivityForResult(intent, ACTION_CAPTURE_IMAGE)
    }

    override fun onPermissionAllow(permissionCode: Int) {
        when (permissionCode) {
            REQUEST_CAPTURE_PERM -> captureImageNew()
        }
    }

    override fun onPermissionDeny(permissionCode: Int) {
        when (permissionCode) {
            REQUEST_CAPTURE_PERM -> {
                Toast.makeText(activity, R.string.msg_permission_denied, Toast.LENGTH_SHORT).show()
                closeImagePicker()
            }
        }
    }


    interface ImagePickerListener {
        fun onImageSelected(requestId: Int, uri: String?, imageName: String?)
        fun onImagePickerClose()
    }

    companion object {
        const val TAG = "ImagePickerDialog"
        private const val ACTION_CHOOSE_IMAGE = 873
        private const val ACTION_CAPTURE_IMAGE = 922
        private const val REQUEST_CAPTURE_PERM = 736
        private const val KEY_PICKER_REQUEST_CODE = "picker_request_code"
        private const val KEY_PICKER_TYPE = "picker_type" //0 - camera only, 1 - gallary only , 2 - both
        private const val KEY_INITIAL_NAME = "intial_name"

    }

    fun newInstance(requestId: Int, initialName: String): ImagePickerFragment {
        val args = Bundle()
        args.putInt(KEY_PICKER_REQUEST_CODE, requestId)
        args.putString(KEY_INITIAL_NAME, initialName)
        val fragment = ImagePickerFragment()
        fragment.arguments = args
        return fragment
    }

    fun newInstance(requestId: Int, pickerType: Int, initialName: String?): ImagePickerFragment {
        val args = Bundle()
        args.putInt(KEY_PICKER_REQUEST_CODE, requestId)
        args.putInt(KEY_PICKER_TYPE, pickerType)
        args.putString(KEY_INITIAL_NAME, initialName)
        val fragment = ImagePickerFragment()
        fragment.arguments = args
        return fragment
    }

    fun showLoading() {
        try {
            hideLoading()
            mProgressDialog = CustomProgressDialog(requireContext(), R.style.progress_dialog_text_style)
            mProgressDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideLoading() {
        try {
            if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                mProgressDialog!!.dismiss()
            }
        } catch (e: Exception) {
        }
    }


}
