package com.farmbirdfs.logjfeiowewg.opf.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication
import com.farmbirdfs.logjfeiowewg.opf.presentation.ui.load.FarmBirdLogisticsLoadFragment
import org.koin.android.ext.android.inject

class FarmBirdLogisticsV : Fragment(){

    private lateinit var farmBirdLogisticsPhoto: Uri
    private var farmBirdLogisticsFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val farmBirdLogisticsTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        farmBirdLogisticsFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        farmBirdLogisticsFilePathFromChrome = null
    }

    private val farmBirdLogisticsTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        farmBirdLogisticsFilePathFromChrome?.onReceiveValue(if (it) arrayOf(farmBirdLogisticsPhoto) else null)
        farmBirdLogisticsFilePathFromChrome = null
    }

    private val farmBirdLogisticsDataStore by activityViewModels<FarmBirdLogisticsDataStore>()


    private val farmBirdLogisticsViFun by inject<FarmBirdLogisticsViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        farmBirdLogisticsRegisterBackCallback()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (farmBirdLogisticsDataStore.farmBirdLogisticsIsFirstCreate.not()) {
            return farmBirdLogisticsDataStore.farmBirdLogisticsContainerView
        }
        farmBirdLogisticsDataStore.farmBirdLogisticsIsFirstCreate = false
        farmBirdLogisticsDataStore.farmBirdLogisticsContainerView = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
        }
        return farmBirdLogisticsDataStore.farmBirdLogisticsContainerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "onViewCreated")
        if (farmBirdLogisticsDataStore.farmBirdLogisticsViList.isEmpty()) {
            farmBirdLogisticsCreateRootWebView()
        } else {
            farmBirdLogisticsReuseExistingWebStack()
        }
        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "WebView list size = ${farmBirdLogisticsDataStore.farmBirdLogisticsViList.size}")
    }

    private fun farmBirdLogisticsRegisterBackCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val activeWebView = farmBirdLogisticsDataStore.farmBirdLogisticsView
                if (activeWebView.canGoBack()) {
                    activeWebView.goBack()
                    Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "WebView can go back")
                    return
                }

                val stack = farmBirdLogisticsDataStore.farmBirdLogisticsViList
                if (stack.size <= 1) return

                Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "WebView can`t go back")
                stack.removeAt(stack.lastIndex)
                activeWebView.destroy()

                val previous = stack.last()
                farmBirdLogisticsDataStore.farmBirdLogisticsView = previous
                farmBirdLogisticsAttachWebViewToContainer(previous)
                Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "WebView list size ${stack.size}")
            }
        })
    }

    private fun farmBirdLogisticsCreateRootWebView() {
        val rootWebView = FarmBirdLogisticsVi(
            requireContext(),
            object : FarmBirdLogisticsCallBack {
                override fun farmBirdLogisticsHandleCreateWebWindowRequest(farmBirdLogisticsVi: FarmBirdLogisticsVi) {
                    farmBirdLogisticsHandlePopupWebView(farmBirdLogisticsVi)
                }
            },
            farmBirdLogisticsWindow = requireActivity().window
        ).also { webView ->
            webView.farmBirdLogisticsSetFileChooserHandler { callback ->
                farmBirdLogisticsHandleFileChooser(callback)
            }
        }

        farmBirdLogisticsDataStore.farmBirdLogisticsView = rootWebView
        farmBirdLogisticsDataStore.farmBirdLogisticsViList.add(rootWebView)
        rootWebView.farmBirdLogisticsFLoad(arguments?.getString(FarmBirdLogisticsLoadFragment.FARM_BIRD_LOGISTICS_D).orEmpty())
        farmBirdLogisticsAttachWebViewToContainer(rootWebView)
    }

    private fun farmBirdLogisticsHandlePopupWebView(farmBirdLogisticsVi: FarmBirdLogisticsVi) {
        farmBirdLogisticsDataStore.farmBirdLogisticsViList.add(farmBirdLogisticsVi)
        farmBirdLogisticsDataStore.farmBirdLogisticsView = farmBirdLogisticsVi
        farmBirdLogisticsVi.farmBirdLogisticsSetFileChooserHandler { callback ->
            farmBirdLogisticsHandleFileChooser(callback)
        }
        farmBirdLogisticsAttachWebViewToContainer(farmBirdLogisticsVi)
        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "CreateWebWindowRequest")
        Log.d(
            FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG,
            "WebView list size = ${farmBirdLogisticsDataStore.farmBirdLogisticsViList.size}"
        )
    }

    private fun farmBirdLogisticsReuseExistingWebStack() {
        farmBirdLogisticsDataStore.farmBirdLogisticsViList.forEach { view ->
            view.farmBirdLogisticsSetFileChooserHandler(::farmBirdLogisticsHandleFileChooser)
        }
        val restored = farmBirdLogisticsDataStore.farmBirdLogisticsViList.last()
        farmBirdLogisticsDataStore.farmBirdLogisticsView = restored
        farmBirdLogisticsAttachWebViewToContainer(restored)
    }

    private fun farmBirdLogisticsHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        farmBirdLogisticsFilePathFromChrome = callback

        val listItems = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Launching file picker")
                    farmBirdLogisticsTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Launching camera")
                    farmBirdLogisticsPhoto = farmBirdLogisticsViFun.farmBirdLogisticsSavePhoto()
                    farmBirdLogisticsTakePhoto.launch(farmBirdLogisticsPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                farmBirdLogisticsFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun farmBirdLogisticsAttachWebViewToContainer(w: FarmBirdLogisticsVi) {
        farmBirdLogisticsDataStore.farmBirdLogisticsContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            farmBirdLogisticsDataStore.farmBirdLogisticsContainerView.removeAllViews()
            farmBirdLogisticsDataStore.farmBirdLogisticsContainerView.addView(w)
        }
    }


}