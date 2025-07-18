package keronei.swapper.dashboard

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import keronei.swapper.dashboard.dispatch.ui.theme.SwapperTheme
import keronei.swapper.data.domain.BatteryRequestModel
import java.util.Calendar

@AndroidEntryPoint
class BatteriesRequestDialogFragment : BottomSheetDialogFragment() {

    val dashViewModel: DashboardViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ComposeDialog(requireContext())
    }

    inner class ComposeDialog(context: Context) : BottomSheetDialog(context) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContentView(
                ComposeView(context).apply {
                    setContent {
                        SwapperTheme {
                            FormBottomSheetContent(
                                onCancel = { dismiss() },
                                onSubmit = { batteriesCount, comment ->
                                    val now = kotlinx.datetime.Clock.System.now().toString()

                                    try {
                                        dashViewModel.createRequest(
                                            BatteryRequestModel(
                                                id = 0,
                                                comment = comment,
                                                requestCount = batteriesCount.toInt(),
                                                createdTime = now,
                                                requestedByStation = "Mpigi",
                                                synced = false,
                                                status = "New"
                                            )
                                        )
                                        Toast.makeText(context, "Submitted!", Toast.LENGTH_SHORT).show()
                                        dismiss()
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}
