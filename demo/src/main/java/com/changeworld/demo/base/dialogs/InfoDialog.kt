package com.changeworld.demo.base.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.changeworld.easynearby.ConnectionStrategy
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.demo.R
import com.changeworld.demo.databinding.FragmentBaseInfoDialogBinding

class InfoDialog : DialogFragment() {

    private var _binding: FragmentBaseInfoDialogBinding? = null
    private val binding get() = _binding!!

    private var dismissIntentionally = false

    private var listener : ((DeviceInfo?) -> Unit)? = null


    fun setListener(listener: (DeviceInfo?) -> Unit) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBaseInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameEdtx.setText(android.os.Build.MODEL)
        binding.okBtn.setOnClickListener {
            val name = binding.nameEdtx.text.toString().takeIf { it.isNotBlank() } ?: kotlin.run {
                Toast.makeText(context, "Name is empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val serviceId =
                binding.serviceIdEdtx.text.toString().takeIf { it.isNotBlank() } ?: kotlin.run {
                    Toast.makeText(context, "ServiceId is empty", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

            val strategy = when (binding.strategyRadioGroup.checkedRadioButtonId) {
                R.id.starRb -> ConnectionStrategy.STAR
                R.id.p2pRb -> ConnectionStrategy.POINT_TO_POINT
                R.id.clusterRb -> ConnectionStrategy.CLUSTER
                else -> {
                    Toast.makeText(context, "Strategy is empty", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            setDeviceInfoResult(name, serviceId, strategy)
            dismissIntentionally = true
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (dismissIntentionally.not()) {
            setEmptyResult()
        }
    }

    private fun setEmptyResult() {
        listener?.invoke(null)
    }

    private fun setDeviceInfoResult(name: String, serviceId: String, strategy: ConnectionStrategy) {
        listener?.invoke(DeviceInfo(name, serviceId, strategy))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    data class AdvertiseInfoDialogResult(
        val name: String,
        val serviceId: String,
        val strategy: ConnectionStrategy
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() as String,
            parcel.readString() as String,
            ConnectionStrategy.valueOf(parcel.readString() as String)
        )

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(name)
            dest.writeString(serviceId)
            dest.writeString(strategy.name)
        }

        companion object CREATOR : Parcelable.Creator<AdvertiseInfoDialogResult> {
            override fun createFromParcel(parcel: Parcel): AdvertiseInfoDialogResult {
                return AdvertiseInfoDialogResult(parcel)
            }

            override fun newArray(size: Int): Array<AdvertiseInfoDialogResult?> {
                return arrayOfNulls(size)
            }
        }
    }
}