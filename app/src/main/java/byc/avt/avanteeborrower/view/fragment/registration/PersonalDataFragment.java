package byc.avt.avanteeborrower.view.fragment.registration;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import byc.avt.avanteeborrower.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PersonalDataFragment extends Fragment {

    private Button next;
    private String[] gender = {"Laki - laki", "Perempuan"};

    public PersonalDataFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.personal_data));
//        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24px);
        next = view.findViewById(R.id.btn_next_personal_info);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = PersonalDataFragmentDirections.actionWorkInfo();
                Navigation.findNavController(next).navigate(action);
            }
        });
    }
}
