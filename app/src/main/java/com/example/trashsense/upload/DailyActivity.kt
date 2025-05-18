package com.example.trashsense.upload

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import com.example.trashsense.R

class DailyActivity : Fragment() {

    private lateinit var scaleAnim: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_daily_activity, container, false)

        // Load animation
        scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.item_click)

        // List of (View ID, Icon Resource, Description Text)
        val items = listOf(
            Triple(
                R.id.DailyActivities_PickedUpTrash,
                R.drawable.ic_trash,
                "I dedicated a few minutes to pick up litter around my neighborhood, making our shared spaces cleaner, safer, and a little more beautiful for everyone who passes by. 🌳🗑️✨"
            ),
            Triple(
                R.id.DailyActivities_HelpedTheAnimals,
                R.drawable.ic_animals,
                "I spent time caring for a stray or volunteering at a local shelter, giving vulnerable animals the love, food, and comfort they deserve while reminding others that compassion has no bounds. 🐾❤️"
            ),
            Triple(
                R.id.DailyActivities_UpcycledRenewed,
                R.drawable.ic_upcycle,
                "I transformed something old or broken into a renewed, functional treasure—proving that creativity can turn waste into wonder and keep valuable materials out of the landfill. ♻️🛠️"
            ),
            Triple(
                R.id.DailyActivities_GrownOwnGroceries,
                R.drawable.ic_growownngroceries,
                "I harvested fresh produce from my own garden (or balcony pots!), reducing the carbon footprint of my meals and reconnecting with the earth’s natural rhythms one bite at a time. 🌱🥬🌞"
            ),
            Triple(
                R.id.DailyActivities_BoughtOrganicProduct,
                R.drawable.ic_organic,
                "I consciously chose an organic, sustainably produced product, supporting farmers who protect soil health, biodiversity, and my own wellbeing with every wholesome bite. 🥕🌿"
            ),
            Triple(
                R.id.DailyActivities_MyOwnEcoActivity,
                R.drawable.ic_eco,
                "Today I carried out my very own eco-friendly action—big or small—that contributes to a healthier planet. Describe what you did here and inspire others with your unique green act! 🌍💚"
            )
        )

        // Set up click listeners
        items.forEach { (viewId, iconResId, descriptionText) ->
            val activityItem = view.findViewById<LinearLayout>(viewId)
            activityItem.setOnClickListener {
                activityItem.startAnimation(scaleAnim)

                scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        val bundle = Bundle().apply {
                            putInt("icon_id", iconResId)
                            putString("descriptiontext", descriptionText)
                        }

                        val uploadFragment = UploadFragment().apply {
                            arguments = bundle
                        }

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.flFragment, uploadFragment)
                            .addToBackStack(null)
                            .commit()

                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }

        return view
    }
}
