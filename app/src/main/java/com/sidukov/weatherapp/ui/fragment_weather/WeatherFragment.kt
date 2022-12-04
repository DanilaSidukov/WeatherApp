package com.sidukov.weatherapp.ui.fragment_weather

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sidukov.weatherapp.R
import com.sidukov.weatherapp.data.Location
import com.sidukov.weatherapp.data.LocationRepository
import com.sidukov.weatherapp.data.MiniCardViewRepository
import com.sidukov.weatherapp.data.WeatherRepository
import com.sidukov.weatherapp.ui.fragment_location.LocationFragment
import com.sidukov.weatherapp.ui.fragment_location.LocationViewAdapter
import com.sidukov.weatherapp.ui.fragment_location.LocationItemDecoration
import com.sidukov.weatherapp.ui.fragment_location.LocationModel
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

class WeatherFragment : Fragment() {

    private lateinit var dailyWeatherRecyclerView: RecyclerView

    private lateinit var buttonEdit: Button

    //adapter привязывается к RecyclerView, он содержит в себе инфу об элементах в списке RecyclerView
    private val adapterDailyWeather = DailyWeatherAdapter(emptyList())

    //Объявляю о том, что будет vm
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var animation: HeaderAnimationImage
    private lateinit var nestedScrollView: CustomScrollView

    private val adapterMiniCardView = WeatherDescriptionCardAdapter(emptyList())
    private lateinit var miniCardViewModel: MiniCardViewModel
    private lateinit var cardViewRecyclerView: RecyclerView

    //Создётся менеджер Корутины (scope), CoroutineScope возвращает Корутину, Dispatchers.Main - область, в которой будет работать Корутина
    //Main обозначает, что будет выполняться это в главном потоке (где рисуются элементы, запускаются анимации..)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Возвращает View всего xml(fragment_weather)
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    //Вызывается после создания фрагмента (View)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        //инициализация vm непосредственно
        weatherViewModel = WeatherViewModel(WeatherRepository(requireContext()))
        //запускается Корутина с помощью launch, scope.launch выполняется асинхронно относительно общего порядка выполнения кода
        //В collect мы указываем что делать с теми данными, которые придут. Выполняется collect каждый раз, когда в weatherList появляются новые данные
        dailyWeatherRecyclerView = view.findViewById(R.id.recycler_view_weather)
        //привязываем adapter к RecycleView
        dailyWeatherRecyclerView.adapter = adapterDailyWeather
        dailyWeatherRecyclerView.addItemDecoration(EmptyDividerItemDecoration())

        OverScrollDecoratorHelper.setUpOverScroll(
            dailyWeatherRecyclerView,
            OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
        )

        miniCardViewModel = MiniCardViewModel(MiniCardViewRepository())
        cardViewRecyclerView = view.findViewById(R.id.card_view_recycler_view)
        cardViewRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        cardViewRecyclerView.adapter = adapterMiniCardView
        cardViewRecyclerView.addItemDecoration(MiniCardViewItemDecoration(16))

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            weatherViewModel.weatherList.collect {
                adapterDailyWeather.updateList(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            miniCardViewModel.cardViewList.collect {
                adapterMiniCardView.updateList(it)
            }
        }

        val animatedImage: View = view.findViewById(R.id.imageSky)
        //Вызываю класс, который отвечает за анимацию заглавного изображения
        animatedImage.viewTreeObserver.addOnGlobalLayoutListener {
            if (!this::animation.isInitialized) {
                animation = HeaderAnimationImage(animatedImage)
            }
            animation.marginFlow = animatedImage.width
        }

        buttonEdit = view.findViewById(R.id.button_edit)
        buttonEdit.setOnClickListener {
            val locationFragment = LocationFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, locationFragment)
            transaction.commit()
        }
    }
}

