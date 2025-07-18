package keronei.swapper.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import keronei.swapper.R
import keronei.swapper.auth.AuthDialogFragment
import keronei.swapper.auth.NavigatorInterface
import keronei.swapper.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class DashboardFragment : Fragment(), NavigatorInterface {

    lateinit var _binding: FragmentDashboardBinding
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.loggedIn) {
            val dialog = AuthDialogFragment()

            dialog.show(childFragmentManager, "checking")
        }

        lifecycleScope.launchWhenCreated {
            viewModel.requests.collect { items ->
                val unsyncedCount = items.count { !it.request.synced }

                withContext(Dispatchers.Main){
                    binding.unsyncedCount.text = "$unsyncedCount unsynced requests"
                }
            }
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.switch_role -> {
                        findNavController().navigate(R.id.assignedRequestsFragment)
                        true
                    }

                    R.id.action_request_batteries -> {
                        BatteriesRequestDialogFragment().show(parentFragmentManager, "FormBottomSheet")
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater)

        return binding.root
    }

    override fun stepToLocationVerification() {
        findNavController().navigate(R.id.locationVerificationFragment)
    }
}