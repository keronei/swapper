package keronei.swapper.auth

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import keronei.swapper.databinding.FragmentItemListDialogListDialogBinding

class AuthDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentItemListDialogListDialogBinding? = null

    private var navigatorInterface: NavigatorInterface? = null

    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)

        navigatorInterface = if (context is NavigatorInterface) {
            context
        } else {
            if (parentFragment is NavigatorInterface) {
                parentFragment as NavigatorInterface
            } else {
                throw RuntimeException("$context or parentFragment must implement FragmentSwitcher")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isHideable = false
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }

        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentItemListDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startCheckIn.setOnClickListener {
            dismiss()
            navigatorInterface?.stepToLocationVerification()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}