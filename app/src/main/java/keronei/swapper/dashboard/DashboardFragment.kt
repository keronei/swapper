package keronei.swapper.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import keronei.swapper.R
import keronei.swapper.auth.AuthDialogFragment
import keronei.swapper.auth.NavigatorInterface

class DashboardFragment : Fragment(), NavigatorInterface {

    companion object {
        fun newInstance() = DashboardFragment()
    }

    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = AuthDialogFragment()
        dialog.show(childFragmentManager, "checking")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun stepToLocationVerification() {
        findNavController().navigate(R.id.locationVerificationFragment)
    }
}