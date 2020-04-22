package ru.skillbranch.avatarimageview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ru.skillbranch.avatarimageview.ui.custom.AvatarImageView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_border.setOnClickListener{
            avi.setBorderWidth((2..10).random())
        }
        btn_color.setOnClickListener{
            avi.setBorderColor((AvatarImageView.bgColors).random())
        }
    }
}
