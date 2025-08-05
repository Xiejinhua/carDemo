package com.desaysv.psmap.ui.ahatrip

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autonavi.gbl.layer.model.BizCustomTypeLine
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMyAhaTankDetailBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.MyAhaTankDetailDayAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * 轨迹路书详情
 */
@AndroidEntryPoint
class MyAhaTankDetailFragment : Fragment() {
    private lateinit var binding: FragmentMyAhaTankDetailBinding
    private val viewModel: MyAhaTankDetailViewModel by viewModels()
    private var myAhaTankDetailDayAdapter: MyAhaTankDetailDayAdapter? = null
    private var position = 0 //对应的位置
    private var isMineFav = false //是否是路书收藏进来的

    @Inject
    lateinit var mSpeechSynthesizeBusiness: SpeechSynthesizeBusiness

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toast: ToastUtil

    override fun onResume() {
        super.onResume()
        viewModel.setTankNodePointLine() //轨迹详情图层扎标和画线
    }

    override fun onPause() {
        super.onPause()
        viewModel.removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine3.toLong())
        viewModel.removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint3.toLong())
        viewModel.removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
        viewModel.exitPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        myAhaTankDetailDayAdapter = null
        viewModel.tankDetail.postValue(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyAhaTankDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.setTankId(arguments?.getInt("id", 0).toString())
        position = arguments?.getInt("position", 0) ?: 0
        isMineFav = arguments?.getBoolean("isMineFav", false) ?: false
        if (viewModel.tankDetail.value == null){
            viewModel.requestMineGuideDetail() //共创路书详情
        }
        myAhaTankDetailDayAdapter = MyAhaTankDetailDayAdapter().also { binding.nodeList.adapter = it }
        KeyboardUtil.hideKeyboard(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            mSpeechSynthesizeBusiness.nowStopPlay() //立即停止播报
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        //播报
        binding.sound.setDebouncedOnClickListener {
            viewModel.tankDetail.value?.description?.let {
                if (!mSpeechSynthesizeBusiness.isPlaying()){
                    mSpeechSynthesizeBusiness.synthesize(it, false)
                }
            }
        }
        ViewClickEffectUtils.addClickScale(binding.sound, CLICKED_SCALE_90)

        //轨迹路书收藏操作
        binding.favorite.setDebouncedOnClickListener {
            mSpeechSynthesizeBusiness.nowStopPlay() //立即停止播报
            if (viewModel.isLogin()){
                viewModel.requestFavorite(viewModel.tankDetail.value?.id.toString(), isMineFav) //路书请求收藏/取消收藏
            } else {
                viewModel.registerLogin()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.favorite, CLICKED_SCALE_95)

        //进去景点详情
        myAhaTankDetailDayAdapter?.setOnItemClickListener { _, _, _ ->
            mSpeechSynthesizeBusiness.nowStopPlay() //立即停止播报
        }

        viewModel.tankDetail.unPeek().observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.setDescriptionData(it)
                val markers = it.markers
                if (!markers.isNullOrEmpty()){
                    myAhaTankDetailDayAdapter?.onRefreshData(markers)
                }
                viewModel.hasNode.postValue(!markers.isNullOrEmpty())
                viewModel.setTankNodePointLine() //轨迹详情图层扎标和画线
            }
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }

        viewModel.isTankFavChange.unPeek().observe(viewLifecycleOwner) {
            try {
                if (isMineFav){
                    if (!it){
                        viewModel.deleteTankCollectResult.postValue(viewModel.tankCollectList.value?.get(position)?.id.toString())
                    }else {
                        viewModel.deleteTankCollectResult.postValue("")
                    }
                } else {
                    viewModel.tankList.value?.get(position)?.isFav  = it
                }
            }catch (e: Exception){
                Timber.i("isTankFavChange Exception:${e.message}")
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            myAhaTankDetailDayAdapter?.notifyDataSetChanged()
        }
    }
}