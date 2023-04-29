package id.my.dsm.routemate.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.repo.OnDistanceRepositoryUpdate;
import id.my.dsm.routemate.data.event.view.OnBottomSheetStateChanged;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.databinding.FragmentDistancesBinding;
import id.my.dsm.routemate.library.dsmlib.DSMSolver;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.ui.fragment.viewmodel.DistancesViewModel;
import id.my.dsm.routemate.ui.model.OptionsMenu;
import id.my.dsm.routemate.ui.recyclerview.DistanceRecViewAdapter;
import id.my.dsm.routemate.usecase.distance.RequestMapboxDistanceMatrixUseCase;

@AndroidEntryPoint
public class DistancesFragment extends Fragment {

    private static final String TAG = DistancesFragment.class.getName();
    private FragmentDistancesBinding binding;

    // Dependencies
    @Inject
    PlaceRepositoryN placeRepository;
    @Inject
    DistanceRepositoryN distanceRepository;
    private RecyclerView rvVehiclesList;
    private DistancesViewModel mViewModel;
    private DistanceRecViewAdapter adapter;

    // Use case
    @Inject
    RequestMapboxDistanceMatrixUseCase requestMapboxDistanceMatrixUseCase;

    public static DistancesFragment newInstance() {
        return new DistancesFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDistancesBinding.inflate(inflater, container, false);

        mViewModel = new ViewModelProvider(requireActivity()).get(DistancesViewModel.class);

        // Setup layout transition
        binding.layoutDistances.getLayoutTransition().setAnimateParentHierarchy(false);
        binding.cardDistancesOverviewLayout.getLayoutTransition().setAnimateParentHierarchy(false);

        // Recycler
        rvVehiclesList = binding.rvDistancesList;
        rvVehiclesList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        adapter = new DistanceRecViewAdapter(distanceRepository.getRecords(), placeRepository, NavHostFragment.findNavController(this));

        rvVehiclesList.setAdapter(adapter);

        // Observe state
        distanceRepository.getRecordsCountObservable().observe(getViewLifecycleOwner(), count -> {

            // Card Overview
            binding.textDistancesNumber.setText(count + " elements");

            boolean condition = distanceRepository.getRecordsCount() > 0;

            binding.imageDistancesStatusDone.setVisibility(condition ? VISIBLE : GONE);
            binding.imageDistancesStatusError.setVisibility(condition ? GONE : VISIBLE);
            binding.textDistancesStatus.setText(condition ? "Done" : "Need Action");

        });

        // Set up listeners
        binding.buttonDistancesOptions.setOnClickListener(v -> {
            // Show optimization menu options
            OptionsMenu optionsMenu = new OptionsMenu(v.getContext(), v, R.menu.menu_popup_distances);

            // Set listeners
            optionsMenu.getPopupMenu().setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.distances_symmetrize:
                        List<MatrixElement> symmetricalMatrix = DSMSolver.symmetrizeMatrix(distanceRepository.getRecords());
                        distanceRepository.setRecords(symmetricalMatrix, false);
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.distances_calculate_matrix:
                        distanceRepository.calculateAirDistances(placeRepository.getRecords(), false);

                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.distances_calculate_matrix_mapbox:
                        requestMapboxDistanceMatrixUseCase.invoke(Vehicle.MapboxProfile.DRIVING.toDirectionsCriteria(), false);
                        break;
                    case R.id.distances_calculate_saving:
                        DSMSolver.calculateDistanceSavingValue(placeRepository.getDSMPlaces(), distanceRepository.getRecords());
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.distances_clear_matrix:
                        distanceRepository.clearRecord();
                        break;
                    case R.id.distances_use_sample_data:

                        // JsonLine produced by excel to json tools online for the sample data https://beautifytools.com/excel-to-json-converter.php and convert to single line https://w3percentagecalculator.com/json-to-one-line-converter/
//                        String sampleMatrixJsonLine = "{\"Matrix\":[{\"0\":\"0\",\"1\":\"1777\",\"2\":\"1638\",\"3\":\"3635\",\"4\":\"5291\",\"5\":\"5191\",\"6\":\"6266\",\"7\":\"6412\",\"8\":\"1099\",\"9\":\"2655\",\"10\":\"2997\",\"11\":\"3707\",\"12\":\"3614\",\"13\":\"3506\",\"14\":\"2842\",\"15\":\"3980\",\"16\":\"3602\",\"17\":\"4821\",\"18\":\"3110\",\"19\":\"2253\",\"20\":\"5514\",\"21\":\"3329\",\"22\":\"1708\",\"23\":\"3742\",\"24\":\"2705\"},{\"0\":\"2866\",\"1\":\"0\",\"2\":\"1416\",\"3\":\"5268\",\"4\":\"4167\",\"5\":\"5273\",\"6\":\"6475\",\"7\":\"5141\",\"8\":\"2731\",\"9\":\"4288\",\"10\":\"4629\",\"11\":\"4611\",\"12\":\"3571\",\"13\":\"3699\",\"14\":\"4474\",\"15\":\"4121\",\"16\":\"3559\",\"17\":\"5219\",\"18\":\"4014\",\"19\":\"3158\",\"20\":\"5471\",\"21\":\"1892\",\"22\":\"3528\",\"23\":\"4647\",\"24\":\"2214\"},{\"0\":\"2104\",\"1\":\"1467\",\"2\":\"0\",\"3\":\"4473\",\"4\":\"4439\",\"5\":\"4460\",\"6\":\"5661\",\"7\":\"5492\",\"8\":\"1937\",\"9\":\"3493\",\"10\":\"3835\",\"11\":\"3274\",\"12\":\"2757\",\"13\":\"2886\",\"14\":\"3680\",\"15\":\"3307\",\"16\":\"2745\",\"17\":\"4405\",\"18\":\"2677\",\"19\":\"2287\",\"20\":\"4657\",\"21\":\"2408\",\"22\":\"2734\",\"23\":\"3806\",\"24\":\"1848\"},{\"0\":\"3588\",\"1\":\"5916\",\"2\":\"5777\",\"3\":\"0\",\"4\":\"7724\",\"5\":\"4913\",\"6\":\"5692\",\"7\":\"10551\",\"8\":\"4965\",\"9\":\"2602\",\"10\":\"1752\",\"11\":\"3453\",\"12\":\"6042\",\"13\":\"4327\",\"14\":\"1491\",\"15\":\"3541\",\"16\":\"6031\",\"17\":\"5154\",\"18\":\"2856\",\"19\":\"4424\",\"20\":\"5532\",\"21\":\"7468\",\"22\":\"3290\",\"23\":\"2651\",\"24\":\"5959\"},{\"0\":\"5457\",\"1\":\"4150\",\"2\":\"4070\",\"3\":\"7680\",\"4\":\"0\",\"5\":\"2959\",\"6\":\"3001\",\"7\":\"3001\",\"8\":\"5578\",\"9\":\"7134\",\"10\":\"7334\",\"11\":\"4651\",\"12\":\"1766\",\"13\":\"3712\",\"14\":\"7179\",\"15\":\"4134\",\"16\":\"1895\",\"17\":\"2760\",\"18\":\"4929\",\"19\":\"4748\",\"20\":\"2577\",\"21\":\"3274\",\"22\":\"6529\",\"23\":\"5620\",\"24\":\"2674\"},{\"0\":\"5522\",\"1\":\"5321\",\"2\":\"4411\",\"3\":\"4916\",\"4\":\"2965\",\"5\":\"0\",\"6\":\"1364\",\"7\":\"5556\",\"8\":\"5604\",\"9\":\"6346\",\"10\":\"5495\",\"11\":\"3108\",\"12\":\"2738\",\"13\":\"1936\",\"14\":\"5340\",\"15\":\"1369\",\"16\":\"2501\",\"17\":\"1083\",\"18\":\"3386\",\"19\":\"4774\",\"20\":\"791\",\"21\":\"4868\",\"22\":\"6605\",\"23\":\"2855\",\"24\":\"3567\"},{\"0\":\"6644\",\"1\":\"6287\",\"2\":\"5534\",\"3\":\"5632\",\"4\":\"3001\",\"5\":\"1364\",\"6\":\"0\",\"7\":\"6554\",\"8\":\"6726\",\"9\":\"7062\",\"10\":\"6212\",\"11\":\"4183\",\"12\":\"3280\",\"13\":\"3011\",\"14\":\"6057\",\"15\":\"2444\",\"16\":\"3043\",\"17\":\"2158\",\"18\":\"4682\",\"19\":\"5896\",\"20\":\"996\",\"21\":\"5410\",\"22\":\"7728\",\"23\":\"3572\",\"24\":\"4810\"},{\"0\":\"6857\",\"1\":\"5154\",\"2\":\"5469\",\"3\":\"9893\",\"4\":\"3001\",\"5\":\"5549\",\"6\":\"7153\",\"7\":\"0\",\"8\":\"7357\",\"9\":\"8913\",\"10\":\"9255\",\"11\":\"7067\",\"12\":\"4356\",\"13\":\"6128\",\"14\":\"9100\",\"15\":\"6550\",\"16\":\"4484\",\"17\":\"5350\",\"18\":\"7345\",\"19\":\"7165\",\"20\":\"5167\",\"21\":\"3498\",\"22\":\"7928\",\"23\":\"8036\",\"24\":\"4191\"},{\"0\":\"1420\",\"1\":\"3197\",\"2\":\"3058\",\"3\":\"3159\",\"4\":\"6379\",\"5\":\"4879\",\"6\":\"5954\",\"7\":\"7833\",\"8\":\"0\",\"9\":\"2179\",\"10\":\"2521\",\"11\":\"3395\",\"12\":\"4697\",\"13\":\"4293\",\"14\":\"2366\",\"15\":\"3669\",\"16\":\"4685\",\"17\":\"5120\",\"18\":\"2798\",\"19\":\"2571\",\"20\":\"5381\",\"21\":\"4749\",\"22\":\"2307\",\"23\":\"3430\",\"24\":\"4125\"},{\"0\":\"2572\",\"1\":\"4349\",\"2\":\"4209\",\"3\":\"2600\",\"4\":\"7237\",\"5\":\"5543\",\"6\":\"6618\",\"7\":\"8984\",\"8\":\"3053\",\"9\":\"0\",\"10\":\"1415\",\"11\":\"4059\",\"12\":\"5555\",\"13\":\"4957\",\"14\":\"1688\",\"15\":\"4332\",\"16\":\"5544\",\"17\":\"5783\",\"18\":\"3462\",\"19\":\"3430\",\"20\":\"6044\",\"21\":\"5900\",\"22\":\"2033\",\"23\":\"4094\",\"24\":\"5276\"},{\"0\":\"3286\",\"1\":\"5063\",\"2\":\"4924\",\"3\":\"2605\",\"4\":\"8358\",\"5\":\"5547\",\"6\":\"6622\",\"7\":\"9698\",\"8\":\"3768\",\"9\":\"971\",\"10\":\"0\",\"11\":\"4063\",\"12\":\"6676\",\"13\":\"4961\",\"14\":\"1692\",\"15\":\"4336\",\"16\":\"6664\",\"17\":\"5787\",\"18\":\"3466\",\"19\":\"4144\",\"20\":\"6048\",\"21\":\"6615\",\"22\":\"2747\",\"23\":\"4098\",\"24\":\"5990\"},{\"0\":\"4484\",\"1\":\"4283\",\"2\":\"3374\",\"3\":\"3820\",\"4\":\"4295\",\"5\":\"1484\",\"6\":\"2559\",\"7\":\"6886\",\"8\":\"4566\",\"9\":\"5250\",\"10\":\"4399\",\"11\":\"0\",\"12\":\"2613\",\"13\":\"898\",\"14\":\"4244\",\"15\":\"273\",\"16\":\"2601\",\"17\":\"1725\",\"18\":\"2348\",\"19\":\"3736\",\"20\":\"1986\",\"21\":\"3625\",\"22\":\"5568\",\"23\":\"1759\",\"24\":\"2530\"},{\"0\":\"3820\",\"1\":\"3633\",\"2\":\"2710\",\"3\":\"6005\",\"4\":\"1772\",\"5\":\"2738\",\"6\":\"3361\",\"7\":\"4363\",\"8\":\"3902\",\"9\":\"5459\",\"10\":\"5659\",\"11\":\"2976\",\"12\":\"0\",\"13\":\"2037\",\"14\":\"5504\",\"15\":\"2459\",\"16\":\"445\",\"17\":\"2105\",\"18\":\"3254\",\"19\":\"3072\",\"20\":\"2356\",\"21\":\"3675\",\"22\":\"4904\",\"23\":\"3945\",\"24\":\"1882\"},{\"0\":\"4329\",\"1\":\"4128\",\"2\":\"3219\",\"3\":\"4644\",\"4\":\"3763\",\"5\":\"1565\",\"6\":\"2640\",\"7\":\"6354\",\"8\":\"4411\",\"9\":\"6074\",\"10\":\"5224\",\"11\":\"2504\",\"12\":\"2081\",\"13\":\"0\",\"14\":\"5069\",\"15\":\"1098\",\"16\":\"2069\",\"17\":\"1315\",\"18\":\"2782\",\"19\":\"3581\",\"20\":\"2067\",\"21\":\"3470\",\"22\":\"5413\",\"23\":\"2584\",\"24\":\"2374\"},{\"0\":\"2788\",\"1\":\"4565\",\"2\":\"4426\",\"3\":\"1491\",\"4\":\"7365\",\"5\":\"4554\",\"6\":\"5629\",\"7\":\"9200\",\"8\":\"3398\",\"9\":\"1689\",\"10\":\"839\",\"11\":\"3070\",\"12\":\"5683\",\"13\":\"3968\",\"14\":\"0\",\"15\":\"3343\",\"16\":\"5672\",\"17\":\"4795\",\"18\":\"2473\",\"19\":\"3646\",\"20\":\"5056\",\"21\":\"6117\",\"22\":\"2377\",\"23\":\"3105\",\"24\":\"5600\"},{\"0\":\"4369\",\"1\":\"4169\",\"2\":\"3259\",\"3\":\"3543\",\"4\":\"4180\",\"5\":\"1369\",\"6\":\"2444\",\"7\":\"6771\",\"8\":\"4451\",\"9\":\"4973\",\"10\":\"4123\",\"11\":\"1955\",\"12\":\"2498\",\"13\":\"783\",\"14\":\"3968\",\"15\":\"0\",\"16\":\"2487\",\"17\":\"1610\",\"18\":\"2233\",\"19\":\"3621\",\"20\":\"1871\",\"21\":\"3510\",\"22\":\"5297\",\"23\":\"1483\",\"24\":\"2415\"},{\"0\":\"3809\",\"1\":\"3622\",\"2\":\"2698\",\"3\":\"5993\",\"4\":\"1901\",\"5\":\"2501\",\"6\":\"3123\",\"7\":\"4492\",\"8\":\"3891\",\"9\":\"5447\",\"10\":\"5647\",\"11\":\"2964\",\"12\":\"445\",\"13\":\"2025\",\"14\":\"5492\",\"15\":\"2447\",\"16\":\"0\",\"17\":\"1778\",\"18\":\"3242\",\"19\":\"3061\",\"20\":\"2119\",\"21\":\"3804\",\"22\":\"4892\",\"23\":\"3933\",\"24\":\"1871\"},{\"0\":\"5281\",\"1\":\"5080\",\"2\":\"4171\",\"3\":\"4868\",\"4\":\"2793\",\"5\":\"795\",\"6\":\"1870\",\"7\":\"5384\",\"8\":\"5363\",\"9\":\"6298\",\"10\":\"5448\",\"11\":\"3060\",\"12\":\"2092\",\"13\":\"1888\",\"14\":\"5293\",\"15\":\"1322\",\"16\":\"1766\",\"17\":\"0\",\"18\":\"3338\",\"19\":\"4533\",\"20\":\"1697\",\"21\":\"4695\",\"22\":\"6365\",\"23\":\"2808\",\"24\":\"3327\"},{\"0\":\"5126\",\"1\":\"4925\",\"2\":\"4015\",\"3\":\"4461\",\"4\":\"4936\",\"5\":\"2126\",\"6\":\"3201\",\"7\":\"7527\",\"8\":\"5208\",\"9\":\"5417\",\"10\":\"4567\",\"11\":\"642\",\"12\":\"3255\",\"13\":\"1540\",\"14\":\"4412\",\"15\":\"915\",\"16\":\"3243\",\"17\":\"2366\",\"18\":\"0\",\"19\":\"4378\",\"20\":\"2627\",\"21\":\"4267\",\"22\":\"5741\",\"23\":\"2401\",\"24\":\"3171\"},{\"0\":\"2512\",\"1\":\"3501\",\"2\":\"2538\",\"3\":\"2986\",\"4\":\"4661\",\"5\":\"2915\",\"6\":\"3990\",\"7\":\"6899\",\"8\":\"2594\",\"9\":\"3676\",\"10\":\"2826\",\"11\":\"1431\",\"12\":\"2979\",\"13\":\"2329\",\"14\":\"2671\",\"15\":\"1704\",\"16\":\"2967\",\"17\":\"3156\",\"18\":\"834\",\"19\":\"0\",\"20\":\"3417\",\"21\":\"3815\",\"22\":\"3596\",\"23\":\"1920\",\"24\":\"2895\"},{\"0\":\"5720\",\"1\":\"5363\",\"2\":\"4610\",\"3\":\"5537\",\"4\":\"2583\",\"5\":\"791\",\"6\":\"996\",\"7\":\"5174\",\"8\":\"5802\",\"9\":\"6967\",\"10\":\"6116\",\"11\":\"3609\",\"12\":\"2356\",\"13\":\"2437\",\"14\":\"5961\",\"15\":\"1871\",\"16\":\"2119\",\"17\":\"1672\",\"18\":\"3887\",\"19\":\"4972\",\"20\":\"0\",\"21\":\"4486\",\"22\":\"6804\",\"23\":\"3476\",\"24\":\"3886\"},{\"0\":\"3865\",\"1\":\"1865\",\"2\":\"2478\",\"3\":\"6901\",\"4\":\"3289\",\"5\":\"4891\",\"6\":\"5514\",\"7\":\"3491\",\"8\":\"4365\",\"9\":\"5922\",\"10\":\"6263\",\"11\":\"4028\",\"12\":\"2960\",\"13\":\"3089\",\"14\":\"6108\",\"15\":\"3510\",\"16\":\"2949\",\"17\":\"4693\",\"18\":\"4306\",\"19\":\"4125\",\"20\":\"4509\",\"21\":\"0\",\"22\":\"4936\",\"23\":\"4996\",\"24\":\"1603\"},{\"0\":\"985\",\"1\":\"2762\",\"2\":\"2623\",\"3\":\"3557\",\"4\":\"6276\",\"5\":\"5277\",\"6\":\"6352\",\"7\":\"7398\",\"8\":\"1021\",\"9\":\"2577\",\"10\":\"2919\",\"11\":\"3793\",\"12\":\"4599\",\"13\":\"4691\",\"14\":\"2764\",\"15\":\"4066\",\"16\":\"4587\",\"17\":\"5518\",\"18\":\"3196\",\"19\":\"2969\",\"20\":\"5779\",\"21\":\"4314\",\"22\":\"0\",\"23\":\"3828\",\"24\":\"3690\"},{\"0\":\"3848\",\"1\":\"5106\",\"2\":\"4967\",\"3\":\"2385\",\"4\":\"6000\",\"5\":\"3189\",\"6\":\"3968\",\"7\":\"8591\",\"8\":\"4155\",\"9\":\"4071\",\"10\":\"3221\",\"11\":\"2288\",\"12\":\"4318\",\"13\":\"2604\",\"14\":\"3066\",\"15\":\"1817\",\"16\":\"4307\",\"17\":\"3430\",\"18\":\"1691\",\"19\":\"3614\",\"20\":\"3808\",\"21\":\"5330\",\"22\":\"4395\",\"23\":\"0\",\"24\":\"4235\"},{\"0\":\"3363\",\"1\":\"2253\",\"2\":\"1975\",\"3\":\"5920\",\"4\":\"2675\",\"5\":\"3526\",\"6\":\"4728\",\"7\":\"4589\",\"8\":\"3376\",\"9\":\"4932\",\"10\":\"5274\",\"11\":\"2891\",\"12\":\"1824\",\"13\":\"1952\",\"14\":\"5119\",\"15\":\"2374\",\"16\":\"1812\",\"17\":\"3472\",\"18\":\"3169\",\"19\":\"2988\",\"20\":\"3723\",\"21\":\"1603\",\"22\":\"4434\",\"23\":\"3860\",\"24\":\"0\"}]}";
                        String sampleMatrixJsonLine = "{\"Matrix\":[{\"0\":\"0\",\"1\":\"1777\",\"2\":\"1638\",\"3\":\"3635\",\"4\":\"5291\",\"5\":\"5191\",\"6\":\"6266\",\"7\":\"6412\",\"8\":\"1099\",\"9\":\"2655\",\"10\":\"2997\",\"11\":\"3707\",\"12\":\"3614\",\"13\":\"3506\",\"14\":\"2842\",\"15\":\"3980\",\"16\":\"3602\",\"17\":\"4821\",\"18\":\"3110\",\"19\":\"2253\",\"20\":\"5514\",\"21\":\"3329\",\"22\":\"1708\",\"23\":\"3742\",\"24\":\"2705\"},{\"0\":\"1777\",\"1\":\"0\",\"2\":\"1416\",\"3\":\"5268\",\"4\":\"4167\",\"5\":\"5273\",\"6\":\"6475\",\"7\":\"5141\",\"8\":\"2731\",\"9\":\"4288\",\"10\":\"4629\",\"11\":\"4611\",\"12\":\"3571\",\"13\":\"3699\",\"14\":\"4474\",\"15\":\"4121\",\"16\":\"3559\",\"17\":\"5219\",\"18\":\"4014\",\"19\":\"3158\",\"20\":\"5471\",\"21\":\"1892\",\"22\":\"3528\",\"23\":\"4647\",\"24\":\"2214\"},{\"0\":\"1638\",\"1\":\"1416\",\"2\":\"0\",\"3\":\"4473\",\"4\":\"4439\",\"5\":\"4460\",\"6\":\"5661\",\"7\":\"5492\",\"8\":\"1937\",\"9\":\"3493\",\"10\":\"3835\",\"11\":\"3274\",\"12\":\"2757\",\"13\":\"2886\",\"14\":\"3680\",\"15\":\"3307\",\"16\":\"2745\",\"17\":\"4405\",\"18\":\"2677\",\"19\":\"2287\",\"20\":\"4657\",\"21\":\"2408\",\"22\":\"2734\",\"23\":\"3806\",\"24\":\"1848\"},{\"0\":\"3635\",\"1\":\"5268\",\"2\":\"4473\",\"3\":\"0\",\"4\":\"7724\",\"5\":\"4913\",\"6\":\"5692\",\"7\":\"10551\",\"8\":\"4965\",\"9\":\"2602\",\"10\":\"1752\",\"11\":\"3453\",\"12\":\"6042\",\"13\":\"4327\",\"14\":\"1491\",\"15\":\"3541\",\"16\":\"6031\",\"17\":\"5154\",\"18\":\"2856\",\"19\":\"4424\",\"20\":\"5532\",\"21\":\"7468\",\"22\":\"3290\",\"23\":\"2651\",\"24\":\"5959\"},{\"0\":\"5291\",\"1\":\"4167\",\"2\":\"4439\",\"3\":\"7724\",\"4\":\"0\",\"5\":\"2959\",\"6\":\"3001\",\"7\":\"3001\",\"8\":\"5578\",\"9\":\"7134\",\"10\":\"7334\",\"11\":\"4651\",\"12\":\"1766\",\"13\":\"3712\",\"14\":\"7179\",\"15\":\"4134\",\"16\":\"1895\",\"17\":\"2760\",\"18\":\"4929\",\"19\":\"4748\",\"20\":\"2577\",\"21\":\"3274\",\"22\":\"6529\",\"23\":\"5620\",\"24\":\"2674\"},{\"0\":\"5191\",\"1\":\"5273\",\"2\":\"4460\",\"3\":\"4913\",\"4\":\"2959\",\"5\":\"0\",\"6\":\"1364\",\"7\":\"5556\",\"8\":\"5604\",\"9\":\"6346\",\"10\":\"5495\",\"11\":\"3108\",\"12\":\"2738\",\"13\":\"1936\",\"14\":\"5340\",\"15\":\"1369\",\"16\":\"2501\",\"17\":\"1083\",\"18\":\"3386\",\"19\":\"4774\",\"20\":\"791\",\"21\":\"4868\",\"22\":\"6605\",\"23\":\"2855\",\"24\":\"3567\"},{\"0\":\"6266\",\"1\":\"6475\",\"2\":\"5661\",\"3\":\"5692\",\"4\":\"3001\",\"5\":\"1364\",\"6\":\"0\",\"7\":\"6554\",\"8\":\"6726\",\"9\":\"7062\",\"10\":\"6212\",\"11\":\"4183\",\"12\":\"3280\",\"13\":\"3011\",\"14\":\"6057\",\"15\":\"2444\",\"16\":\"3043\",\"17\":\"2158\",\"18\":\"4682\",\"19\":\"5896\",\"20\":\"996\",\"21\":\"5410\",\"22\":\"7728\",\"23\":\"3572\",\"24\":\"4810\"},{\"0\":\"6412\",\"1\":\"5141\",\"2\":\"5492\",\"3\":\"10551\",\"4\":\"3001\",\"5\":\"5556\",\"6\":\"6554\",\"7\":\"0\",\"8\":\"7357\",\"9\":\"8913\",\"10\":\"9255\",\"11\":\"7067\",\"12\":\"4356\",\"13\":\"6128\",\"14\":\"9100\",\"15\":\"6550\",\"16\":\"4484\",\"17\":\"5350\",\"18\":\"7345\",\"19\":\"7165\",\"20\":\"5167\",\"21\":\"3498\",\"22\":\"7928\",\"23\":\"8036\",\"24\":\"4191\"},{\"0\":\"1099\",\"1\":\"2731\",\"2\":\"1937\",\"3\":\"4965\",\"4\":\"5578\",\"5\":\"5604\",\"6\":\"6726\",\"7\":\"7357\",\"8\":\"0\",\"9\":\"2179\",\"10\":\"2521\",\"11\":\"3395\",\"12\":\"4697\",\"13\":\"4293\",\"14\":\"2366\",\"15\":\"3669\",\"16\":\"4685\",\"17\":\"5120\",\"18\":\"2798\",\"19\":\"2571\",\"20\":\"5381\",\"21\":\"4749\",\"22\":\"2307\",\"23\":\"3430\",\"24\":\"4125\"},{\"0\":\"2655\",\"1\":\"4288\",\"2\":\"3493\",\"3\":\"2602\",\"4\":\"7134\",\"5\":\"6346\",\"6\":\"7062\",\"7\":\"8913\",\"8\":\"2179\",\"9\":\"0\",\"10\":\"1415\",\"11\":\"4059\",\"12\":\"5555\",\"13\":\"4957\",\"14\":\"1688\",\"15\":\"4332\",\"16\":\"5544\",\"17\":\"5783\",\"18\":\"3462\",\"19\":\"3430\",\"20\":\"6044\",\"21\":\"5900\",\"22\":\"2033\",\"23\":\"4094\",\"24\":\"5276\"},{\"0\":\"2997\",\"1\":\"4629\",\"2\":\"3835\",\"3\":\"1752\",\"4\":\"7334\",\"5\":\"5495\",\"6\":\"6212\",\"7\":\"9255\",\"8\":\"2521\",\"9\":\"1415\",\"10\":\"0\",\"11\":\"4063\",\"12\":\"6676\",\"13\":\"4961\",\"14\":\"1692\",\"15\":\"4336\",\"16\":\"6664\",\"17\":\"5787\",\"18\":\"3466\",\"19\":\"4144\",\"20\":\"6048\",\"21\":\"6615\",\"22\":\"2747\",\"23\":\"4098\",\"24\":\"5990\"},{\"0\":\"3707\",\"1\":\"4611\",\"2\":\"3274\",\"3\":\"3453\",\"4\":\"4651\",\"5\":\"3108\",\"6\":\"4183\",\"7\":\"7067\",\"8\":\"3395\",\"9\":\"4059\",\"10\":\"4063\",\"11\":\"0\",\"12\":\"2613\",\"13\":\"898\",\"14\":\"4244\",\"15\":\"273\",\"16\":\"2601\",\"17\":\"1725\",\"18\":\"2348\",\"19\":\"3736\",\"20\":\"1986\",\"21\":\"3625\",\"22\":\"5568\",\"23\":\"1759\",\"24\":\"2530\"},{\"0\":\"3614\",\"1\":\"3571\",\"2\":\"2757\",\"3\":\"6042\",\"4\":\"1766\",\"5\":\"2738\",\"6\":\"3280\",\"7\":\"4356\",\"8\":\"4697\",\"9\":\"5555\",\"10\":\"6676\",\"11\":\"2613\",\"12\":\"0\",\"13\":\"2037\",\"14\":\"5504\",\"15\":\"2459\",\"16\":\"445\",\"17\":\"2105\",\"18\":\"3254\",\"19\":\"3072\",\"20\":\"2356\",\"21\":\"3675\",\"22\":\"4904\",\"23\":\"3945\",\"24\":\"1882\"},{\"0\":\"3506\",\"1\":\"3699\",\"2\":\"2886\",\"3\":\"4327\",\"4\":\"3712\",\"5\":\"1936\",\"6\":\"3011\",\"7\":\"6128\",\"8\":\"4293\",\"9\":\"4957\",\"10\":\"4961\",\"11\":\"898\",\"12\":\"2037\",\"13\":\"0\",\"14\":\"5069\",\"15\":\"1098\",\"16\":\"2069\",\"17\":\"1315\",\"18\":\"2782\",\"19\":\"3581\",\"20\":\"2067\",\"21\":\"3470\",\"22\":\"5413\",\"23\":\"2584\",\"24\":\"2374\"},{\"0\":\"2842\",\"1\":\"4474\",\"2\":\"3680\",\"3\":\"1491\",\"4\":\"7179\",\"5\":\"5340\",\"6\":\"6057\",\"7\":\"9100\",\"8\":\"2366\",\"9\":\"1688\",\"10\":\"1692\",\"11\":\"4244\",\"12\":\"5504\",\"13\":\"5069\",\"14\":\"0\",\"15\":\"3343\",\"16\":\"5672\",\"17\":\"4795\",\"18\":\"2473\",\"19\":\"3646\",\"20\":\"5056\",\"21\":\"6117\",\"22\":\"2377\",\"23\":\"3105\",\"24\":\"5600\"},{\"0\":\"3980\",\"1\":\"4121\",\"2\":\"3307\",\"3\":\"3541\",\"4\":\"4134\",\"5\":\"1369\",\"6\":\"2444\",\"7\":\"6550\",\"8\":\"3669\",\"9\":\"4332\",\"10\":\"4336\",\"11\":\"273\",\"12\":\"2459\",\"13\":\"1098\",\"14\":\"3343\",\"15\":\"0\",\"16\":\"2487\",\"17\":\"1610\",\"18\":\"2233\",\"19\":\"3621\",\"20\":\"1871\",\"21\":\"3510\",\"22\":\"5297\",\"23\":\"1483\",\"24\":\"2415\"},{\"0\":\"3602\",\"1\":\"3559\",\"2\":\"2745\",\"3\":\"6031\",\"4\":\"1895\",\"5\":\"2501\",\"6\":\"3043\",\"7\":\"4484\",\"8\":\"4685\",\"9\":\"5544\",\"10\":\"6664\",\"11\":\"2601\",\"12\":\"445\",\"13\":\"2069\",\"14\":\"5672\",\"15\":\"2487\",\"16\":\"0\",\"17\":\"1778\",\"18\":\"3242\",\"19\":\"3061\",\"20\":\"2119\",\"21\":\"3804\",\"22\":\"4892\",\"23\":\"3933\",\"24\":\"1871\"},{\"0\":\"4821\",\"1\":\"5219\",\"2\":\"4405\",\"3\":\"5154\",\"4\":\"2760\",\"5\":\"1083\",\"6\":\"2158\",\"7\":\"5350\",\"8\":\"5120\",\"9\":\"5783\",\"10\":\"5787\",\"11\":\"1725\",\"12\":\"2105\",\"13\":\"1315\",\"14\":\"4795\",\"15\":\"1610\",\"16\":\"1778\",\"17\":\"0\",\"18\":\"3338\",\"19\":\"4533\",\"20\":\"1697\",\"21\":\"4695\",\"22\":\"6365\",\"23\":\"2808\",\"24\":\"3327\"},{\"0\":\"3110\",\"1\":\"4014\",\"2\":\"2677\",\"3\":\"2856\",\"4\":\"4929\",\"5\":\"3386\",\"6\":\"4682\",\"7\":\"7345\",\"8\":\"2798\",\"9\":\"3462\",\"10\":\"3466\",\"11\":\"2348\",\"12\":\"3254\",\"13\":\"2782\",\"14\":\"2473\",\"15\":\"2233\",\"16\":\"3242\",\"17\":\"3338\",\"18\":\"0\",\"19\":\"4378\",\"20\":\"2627\",\"21\":\"4267\",\"22\":\"5741\",\"23\":\"2401\",\"24\":\"3171\"},{\"0\":\"2253\",\"1\":\"3158\",\"2\":\"2287\",\"3\":\"4424\",\"4\":\"4748\",\"5\":\"4774\",\"6\":\"5896\",\"7\":\"7165\",\"8\":\"2571\",\"9\":\"3430\",\"10\":\"4144\",\"11\":\"3736\",\"12\":\"3072\",\"13\":\"3581\",\"14\":\"3646\",\"15\":\"3621\",\"16\":\"3061\",\"17\":\"4533\",\"18\":\"4378\",\"19\":\"0\",\"20\":\"3417\",\"21\":\"3815\",\"22\":\"3596\",\"23\":\"1920\",\"24\":\"2895\"},{\"0\":\"5514\",\"1\":\"5471\",\"2\":\"4657\",\"3\":\"5532\",\"4\":\"2577\",\"5\":\"791\",\"6\":\"996\",\"7\":\"5167\",\"8\":\"5381\",\"9\":\"6044\",\"10\":\"6048\",\"11\":\"1986\",\"12\":\"2356\",\"13\":\"2067\",\"14\":\"5056\",\"15\":\"1871\",\"16\":\"2119\",\"17\":\"1697\",\"18\":\"2627\",\"19\":\"3417\",\"20\":\"0\",\"21\":\"4486\",\"22\":\"6804\",\"23\":\"3476\",\"24\":\"3886\"},{\"0\":\"3329\",\"1\":\"1892\",\"2\":\"2408\",\"3\":\"7468\",\"4\":\"3274\",\"5\":\"4868\",\"6\":\"5410\",\"7\":\"3498\",\"8\":\"4749\",\"9\":\"5900\",\"10\":\"6615\",\"11\":\"3625\",\"12\":\"3675\",\"13\":\"3470\",\"14\":\"6117\",\"15\":\"3510\",\"16\":\"3804\",\"17\":\"4695\",\"18\":\"4267\",\"19\":\"3815\",\"20\":\"4486\",\"21\":\"0\",\"22\":\"4936\",\"23\":\"4996\",\"24\":\"1603\"},{\"0\":\"1708\",\"1\":\"3528\",\"2\":\"2734\",\"3\":\"3290\",\"4\":\"6529\",\"5\":\"6605\",\"6\":\"7728\",\"7\":\"7928\",\"8\":\"2307\",\"9\":\"2033\",\"10\":\"2747\",\"11\":\"5568\",\"12\":\"4904\",\"13\":\"5413\",\"14\":\"2377\",\"15\":\"5297\",\"16\":\"4892\",\"17\":\"6365\",\"18\":\"5741\",\"19\":\"3596\",\"20\":\"6804\",\"21\":\"4936\",\"22\":\"0\",\"23\":\"3828\",\"24\":\"3690\"},{\"0\":\"3742\",\"1\":\"4647\",\"2\":\"3806\",\"3\":\"2651\",\"4\":\"5620\",\"5\":\"2855\",\"6\":\"3572\",\"7\":\"8036\",\"8\":\"3430\",\"9\":\"4094\",\"10\":\"4098\",\"11\":\"1759\",\"12\":\"3945\",\"13\":\"2584\",\"14\":\"3105\",\"15\":\"1483\",\"16\":\"3933\",\"17\":\"2808\",\"18\":\"2401\",\"19\":\"1920\",\"20\":\"3476\",\"21\":\"4996\",\"22\":\"3828\",\"23\":\"0\",\"24\":\"4235\"},{\"0\":\"2705\",\"1\":\"2214\",\"2\":\"1848\",\"3\":\"5959\",\"4\":\"2674\",\"5\":\"3567\",\"6\":\"4810\",\"7\":\"4191\",\"8\":\"4125\",\"9\":\"5276\",\"10\":\"5990\",\"11\":\"2530\",\"12\":\"1882\",\"13\":\"2374\",\"14\":\"5600\",\"15\":\"2415\",\"16\":\"1871\",\"17\":\"3327\",\"18\":\"3171\",\"19\":\"2895\",\"20\":\"3886\",\"21\":\"1603\",\"22\":\"3690\",\"23\":\"4235\",\"24\":\"0\"}]}";
//                        String sampleMatrixJsonLine = "{\"Dikri\":[{\"0\":\"0\",\"1\":\"6.6\",\"2\":\"2.4\",\"3\":\"15.7\",\"4\":\"31\",\"5\":\"23.9\",\"6\":\"34.9\",\"7\":\"13.6\",\"8\":\"15.4\",\"9\":\"5.8\",\"10\":\"18.4\",\"11\":\"13.1\",\"12\":\"16.4\",\"13\":\"14.9\",\"14\":\"20\",\"15\":\"6.3\",\"16\":\"14.3\",\"17\":\"18.7\",\"18\":\"43.9\",\"19\":\"31\",\"20\":\"18.8\",\"21\":\"17.4\"},{\"0\":\"6.6\",\"1\":\"0\",\"2\":\"6.1\",\"3\":\"20.6\",\"4\":\"24\",\"5\":\"18.8\",\"6\":\"34\",\"7\":\"17.3\",\"8\":\"19.1\",\"9\":\"4.5\",\"10\":\"12.2\",\"11\":\"16.5\",\"12\":\"21.8\",\"13\":\"8.3\",\"14\":\"13.7\",\"15\":\"16.1\",\"16\":\"10.3\",\"17\":\"22.3\",\"18\":\"37.3\",\"19\":\"24.5\",\"20\":\"12.5\",\"21\":\"11.1\"},{\"0\":\"2.4\",\"1\":\"6.1\",\"2\":\"0\",\"3\":\"15.2\",\"4\":\"29.3\",\"5\":\"22.3\",\"6\":\"28\",\"7\":\"13.1\",\"8\":\"14.9\",\"9\":\"4.1\",\"10\":\"17.1\",\"11\":\"11.9\",\"12\":\"15.9\",\"13\":\"13.2\",\"14\":\"22.2\",\"15\":\"8.6\",\"16\":\"12.6\",\"17\":\"17.9\",\"18\":\"42.2\",\"19\":\"29.4\",\"20\":\"17.1\",\"21\":\"15.8\"},{\"0\":\"15.7\",\"1\":\"20.6\",\"2\":\"15.2\",\"3\":\"0\",\"4\":\"43.9\",\"5\":\"36.8\",\"6\":\"13.4\",\"7\":\"7.6\",\"8\":\"2.8\",\"9\":\"18.6\",\"10\":\"26.3\",\"11\":\"17.2\",\"12\":\"7.8\",\"13\":\"27.8\",\"14\":\"36.7\",\"15\":\"10.9\",\"16\":\"22.3\",\"17\":\"19.1\",\"18\":\"54.4\",\"19\":\"41.6\",\"20\":\"31.7\",\"21\":\"30.3\"},{\"0\":\"31\",\"1\":\"24\",\"2\":\"29.3\",\"3\":\"43.9\",\"4\":\"0\",\"5\":\"38.4\",\"6\":\"51.8\",\"7\":\"41.7\",\"8\":\"43.5\",\"9\":\"27.7\",\"10\":\"16.9\",\"11\":\"31.1\",\"12\":\"46.2\",\"13\":\"21.3\",\"14\":\"33.7\",\"15\":\"40.5\",\"16\":\"19.5\",\"17\":\"35\",\"18\":\"12.9\",\"19\":\"3.7\",\"20\":\"33.3\",\"21\":\"31.9\"},{\"0\":\"23.9\",\"1\":\"18.8\",\"2\":\"22.3\",\"3\":\"36.8\",\"4\":\"38.4\",\"5\":\"0\",\"6\":\"50.3\",\"7\":\"34.7\",\"8\":\"36.5\",\"9\":\"21.9\",\"10\":\"29.5\",\"11\":\"34\",\"12\":\"39.3\",\"13\":\"18.4\",\"14\":\"7.8\",\"15\":\"33.5\",\"16\":\"27.9\",\"17\":\"39\",\"18\":\"51.3\",\"19\":\"38.5\",\"20\":\"5.1\",\"21\":\"10.3\"},{\"0\":\"34.9\",\"1\":\"34\",\"2\":\"28\",\"3\":\"13.4\",\"4\":\"51.8\",\"5\":\"50.3\",\"6\":\"0\",\"7\":\"21.1\",\"8\":\"12.5\",\"9\":\"32.1\",\"10\":\"32.8\",\"11\":\"22.7\",\"12\":\"12\",\"13\":\"39.9\",\"14\":\"50.2\",\"15\":\"24.4\",\"16\":\"28.8\",\"17\":\"16.8\",\"18\":\"60.9\",\"19\":\"48.1\",\"20\":\"45.1\",\"21\":\"42.4\"},{\"0\":\"13.6\",\"1\":\"17.3\",\"2\":\"13.1\",\"3\":\"7.6\",\"4\":\"41.7\",\"5\":\"34.7\",\"6\":\"21.1\",\"7\":\"0\",\"8\":\"8.6\",\"9\":\"16.5\",\"10\":\"24.1\",\"11\":\"15\",\"12\":\"11.2\",\"13\":\"25.6\",\"14\":\"34.6\",\"15\":\"5.9\",\"16\":\"20.1\",\"17\":\"17\",\"18\":\"52.3\",\"19\":\"39.5\",\"20\":\"29.5\",\"21\":\"28.2\"},{\"0\":\"15.4\",\"1\":\"19.1\",\"2\":\"14.9\",\"3\":\"2.8\",\"4\":\"43.5\",\"5\":\"36.5\",\"6\":\"12.5\",\"7\":\"8.6\",\"8\":\"0\",\"9\":\"18.3\",\"10\":\"25.9\",\"11\":\"16.8\",\"12\":\"4.4\",\"13\":\"27.4\",\"14\":\"27.4\",\"15\":\"11.9\",\"16\":\"21.9\",\"17\":\"18.8\",\"18\":\"54.1\",\"19\":\"41.3\",\"20\":\"31.3\",\"21\":\"30\"},{\"0\":\"5.8\",\"1\":\"4.5\",\"2\":\"4.1\",\"3\":\"18.6\",\"4\":\"27.7\",\"5\":\"21.9\",\"6\":\"32.1\",\"7\":\"16.5\",\"8\":\"18.3\",\"9\":\"0\",\"10\":\"13.7\",\"11\":\"11.9\",\"12\":\"20.9\",\"13\":\"12.8\",\"14\":\"21.7\",\"15\":\"12\",\"16\":\"9.7\",\"17\":\"21.6\",\"18\":\"41.7\",\"19\":\"28.9\",\"20\":\"16.7\",\"21\":\"15.3\"},{\"0\":\"18.4\",\"1\":\"12.2\",\"2\":\"17.1\",\"3\":\"26.3\",\"4\":\"16.9\",\"5\":\"29.5\",\"6\":\"32.8\",\"7\":\"24.1\",\"8\":\"25.9\",\"9\":\"13.7\",\"10\":\"0\",\"11\":\"12\",\"12\":\"25.2\",\"13\":\"12.3\",\"14\":\"25.7\",\"15\":\"22.9\",\"16\":\"4\",\"17\":\"16\",\"18\":\"28.1\",\"19\":\"15.3\",\"20\":\"24.7\",\"21\":\"23\"},{\"0\":\"13.1\",\"1\":\"16.5\",\"2\":\"11.9\",\"3\":\"17.2\",\"4\":\"31.1\",\"5\":\"34\",\"6\":\"22.7\",\"7\":\"15\",\"8\":\"16.8\",\"9\":\"11.9\",\"10\":\"12\",\"11\":\"0\",\"12\":\"14.8\",\"13\":\"21.6\",\"14\":\"33.8\",\"15\":\"18\",\"16\":\"8\",\"17\":\"7.8\",\"18\":\"40.1\",\"19\":\"27.3\",\"20\":\"28.7\",\"21\":\"27.4\"},{\"0\":\"16.4\",\"1\":\"21.8\",\"2\":\"15.9\",\"3\":\"7.8\",\"4\":\"46.2\",\"5\":\"39.3\",\"6\":\"12\",\"7\":\"11.2\",\"8\":\"4.4\",\"9\":\"20.9\",\"10\":\"25.2\",\"11\":\"14.8\",\"12\":\"0\",\"13\":\"30.1\",\"14\":\"39.1\",\"15\":\"14.6\",\"16\":\"21.2\",\"17\":\"16.8\",\"18\":\"53.3\",\"19\":\"40.5\",\"20\":\"34\",\"21\":\"32.6\"},{\"0\":\"14.9\",\"1\":\"8.3\",\"2\":\"13.2\",\"3\":\"27.8\",\"4\":\"21.3\",\"5\":\"18.4\",\"6\":\"39.9\",\"7\":\"25.6\",\"8\":\"27.4\",\"9\":\"12.8\",\"10\":\"12.3\",\"11\":\"21.6\",\"12\":\"30.1\",\"13\":\"0\",\"14\":\"14.6\",\"15\":\"24.4\",\"16\":\"13.6\",\"17\":\"25.6\",\"18\":\"34.1\",\"19\":\"21.3\",\"20\":\"13.3\",\"21\":\"11.9\"},{\"0\":\"20\",\"1\":\"13.7\",\"2\":\"22.2\",\"3\":\"36.7\",\"4\":\"33.7\",\"5\":\"7.8\",\"6\":\"50.2\",\"7\":\"34.6\",\"8\":\"27.4\",\"9\":\"21.7\",\"10\":\"25.7\",\"11\":\"33.8\",\"12\":\"39.1\",\"13\":\"14.6\",\"14\":\"0\",\"15\":\"33.5\",\"16\":\"27.9\",\"17\":\"39.8\",\"18\":\"51.3\",\"19\":\"34.7\",\"20\":\"5.1\",\"21\":\"10.2\"},{\"0\":\"6.3\",\"1\":\"16.1\",\"2\":\"8.6\",\"3\":\"10.9\",\"4\":\"40.5\",\"5\":\"33.5\",\"6\":\"24.4\",\"7\":\"5.9\",\"8\":\"11.9\",\"9\":\"12\",\"10\":\"22.9\",\"11\":\"18\",\"12\":\"14.6\",\"13\":\"24.4\",\"14\":\"33.5\",\"15\":\"0\",\"16\":\"18.9\",\"17\":\"19.9\",\"18\":\"51\",\"19\":\"38.2\",\"20\":\"28.3\",\"21\":\"26.9\"},{\"0\":\"14.3\",\"1\":\"10.3\",\"2\":\"12.6\",\"3\":\"22.3\",\"4\":\"19.5\",\"5\":\"27.9\",\"6\":\"28.8\",\"7\":\"20.1\",\"8\":\"21.9\",\"9\":\"9.7\",\"10\":\"4\",\"11\":\"8\",\"12\":\"21.2\",\"13\":\"13.6\",\"14\":\"27.9\",\"15\":\"18.9\",\"16\":\"0\",\"17\":\"12\",\"18\":\"32.1\",\"19\":\"19.3\",\"20\":\"22.8\",\"21\":\"21.4\"},{\"0\":\"18.7\",\"1\":\"22.3\",\"2\":\"17.9\",\"3\":\"19.1\",\"4\":\"35\",\"5\":\"39\",\"6\":\"16.8\",\"7\":\"17\",\"8\":\"18.8\",\"9\":\"21.6\",\"10\":\"16\",\"11\":\"7.8\",\"12\":\"16.8\",\"13\":\"25.6\",\"14\":\"39.8\",\"15\":\"19.9\",\"16\":\"12\",\"17\":\"0\",\"18\":\"44.1\",\"19\":\"31.3\",\"20\":\"34.7\",\"21\":\"33.3\"},{\"0\":\"43.9\",\"1\":\"37.3\",\"2\":\"42.2\",\"3\":\"54.4\",\"4\":\"12.9\",\"5\":\"51.3\",\"6\":\"60.9\",\"7\":\"52.3\",\"8\":\"54.1\",\"9\":\"41.7\",\"10\":\"28.1\",\"11\":\"40.1\",\"12\":\"53.3\",\"13\":\"34.1\",\"14\":\"51.3\",\"15\":\"51\",\"16\":\"32.1\",\"17\":\"44.1\",\"18\":\"0\",\"19\":\"15.2\",\"20\":\"46.2\",\"21\":\"44.8\"},{\"0\":\"31\",\"1\":\"24.5\",\"2\":\"29.4\",\"3\":\"41.6\",\"4\":\"3.7\",\"5\":\"38.5\",\"6\":\"48.1\",\"7\":\"39.5\",\"8\":\"41.3\",\"9\":\"28.9\",\"10\":\"15.3\",\"11\":\"27.3\",\"12\":\"40.5\",\"13\":\"21.3\",\"14\":\"34.7\",\"15\":\"38.2\",\"16\":\"19.3\",\"17\":\"31.3\",\"18\":\"15.2\",\"19\":\"0\",\"20\":\"33.4\",\"21\":\"32\"},{\"0\":\"18.8\",\"1\":\"12.5\",\"2\":\"17.1\",\"3\":\"31.7\",\"4\":\"33.3\",\"5\":\"5.1\",\"6\":\"45.1\",\"7\":\"29.5\",\"8\":\"31.3\",\"9\":\"16.7\",\"10\":\"24.7\",\"11\":\"28.7\",\"12\":\"34\",\"13\":\"13.3\",\"14\":\"5.1\",\"15\":\"28.3\",\"16\":\"22.8\",\"17\":\"34.7\",\"18\":\"46.2\",\"19\":\"33.4\",\"20\":\"0\",\"21\":\"5.2\"},{\"0\":\"17.4\",\"1\":\"11.1\",\"2\":\"15.8\",\"3\":\"30.3\",\"4\":\"31.9\",\"5\":\"10.3\",\"6\":\"42.4\",\"7\":\"28.2\",\"8\":\"30\",\"9\":\"15.3\",\"10\":\"23\",\"11\":\"27.4\",\"12\":\"32.6\",\"13\":\"11.9\",\"14\":\"10.2\",\"15\":\"26.9\",\"16\":\"21.4\",\"17\":\"33.3\",\"18\":\"44.8\",\"19\":\"32\",\"20\":\"5.2\",\"21\":\"0\"}]}";

                        JsonObject sampleMatrixJsonObject = new Gson().fromJson(sampleMatrixJsonLine, JsonObject.class);
                        JsonArray sampleMatrixJsonArray = sampleMatrixJsonObject.getAsJsonArray("Matrix"); // Parse array from member Matrix

                        Log.e(TAG, "UseSampleData: Json size: " + sampleMatrixJsonArray.size() + " | Place size: " + placeRepository.getRecordsCount());

                        ArrayList<Double> matrixDistances = new ArrayList<>();

                        for (int i = 0; i < sampleMatrixJsonArray.size(); i ++) {

                            JsonObject object = sampleMatrixJsonArray.get(i).getAsJsonObject();

                            for (int o = 0; o < placeRepository.getRecordsCount(); o ++) {

                                // Avoid 0 values insertion
                                if (i == o)
                                    continue;

                                Log.e(TAG, "UseSampleData: " + i + " | " + o + " || MatrixElement: " + object.get(String.valueOf(o)).getAsString());
                                matrixDistances.add(object.get(String.valueOf(o)).getAsDouble());

                            }
                        }
                        Log.e(TAG, "UseSampleData: Matrix size: " + matrixDistances.size());

                        // Set distance obtained from sample data to the corresponding instances
                        for (int i = 0; i < matrixDistances.size(); i ++) {
                            distanceRepository.getRecordByIndex(i).setDistance(matrixDistances.get(i));
                        }

                        adapter.notifyDataSetChanged();

                        break;
                    case R.id.distances_show_hint:
                        Toast.makeText(requireContext(), "No hint", Toast.LENGTH_SHORT).show();
                        // TODO: Implement hint
                        break;
                }
                return true;
            });
            optionsMenu.show();
        });

        return binding.getRoot();
    }

    // Subscribe to OnDistanceRepositoryUpdate event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _105705072022(OnDistanceRepositoryUpdate event) {

        switch (event.getStatus()) {

            case RECORD_ADDED:
            case RECORD_DELETED:
            case RECORDS_CLEARED:
                adapter.notifyDataSetChanged();
                break;
        }
    }

    // Subscribe to OnBottomSheetStateChanged event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _073205062022(OnBottomSheetStateChanged event) {

        switch (event.getState()) {
            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_HALF_EXPANDED:
                binding.cardDistancesOverview.setVisibility(VISIBLE);
                break;
            case BottomSheetBehavior.STATE_EXPANDED:
                binding.cardDistancesOverview.setVisibility(GONE);
                break;
        }

    }

}