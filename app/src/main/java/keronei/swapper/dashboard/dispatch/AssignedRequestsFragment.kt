package keronei.swapper.dashboard.dispatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import keronei.swapper.dashboard.DashboardViewModel
import keronei.swapper.dashboard.dispatch.ui.theme.SwapperTheme

@AndroidEntryPoint
class AssignedRequestsFragment : Fragment() {
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SwapperTheme {
                    DispatchLandingScreen(dashboardViewModel, Modifier)
                }
            }
        }
    }
}