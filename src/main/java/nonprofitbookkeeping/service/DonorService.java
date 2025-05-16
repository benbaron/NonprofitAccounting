package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Donor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonorService {

    // In-memory storage for donors
    private List<Donor> donors;

    public DonorService() {
        this.donors = new ArrayList<>();
    }

    // Add a new donor
    public void addDonor(Donor donor) {
        this.donors.add(donor);
    }

    // Edit an existing donor
    public boolean editDonor(String donorName, Donor updatedDonor) {
        Optional<Donor> donorToEdit = this.donors.stream()
                .filter(donor -> donor.getName().equals(donorName))
                .findFirst();

        if (donorToEdit.isPresent()) {
            Donor donor = donorToEdit.get();
            donor.setDonationAmount(updatedDonor.getDonationAmount());
            donor.setDonationType(updatedDonor.getDonationType());
            donor.setDonationDate(updatedDonor.getDonationDate());
            return true;
        }

        return false; // Donor not found
    }

    // Remove a donor by name
    public boolean removeDonor(String donorName) {
        return this.donors.removeIf(donor -> donor.getName().equals(donorName));
    }

    // Get all donors
    public List<Donor> getAllDonors() {
        return new ArrayList<>(this.donors);
    }
}
