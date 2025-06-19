package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Donor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link Donor} information.
 * This class provides functionalities to add, edit, remove, and retrieve donor data.
 * Donor information is stored in an in-memory list.
 */
public class DonorService {

    /** In-memory list to store {@link Donor} objects. */
    private List<Donor> donors;

    /**
     * Constructs a new {@code DonorService}.
     * Initializes an empty list to store donors.
     */
    public DonorService() {
        this.donors = new ArrayList<>();
    }

    /**
     * Adds a new donor to the service.
     *
     * @param donor The {@link Donor} object to add. Must not be null.
     * @throws NullPointerException if {@code donor} is null (due to ArrayList behavior).
     */
    public void addDonor(Donor donor) {
        this.donors.add(donor);
    }

    /**
     * Edits an existing donor's information.
     * The donor to be edited is identified by their name. If found, the donation amount,
     * donation type, and donation date of the existing donor are updated with values
     * from {@code updatedDonor}.
     *
     * @param donorName The name of the donor to edit.
     * @param updatedDonor A {@link Donor} object containing the updated information.
     *                     Only donation amount, type, and date are currently updated from this object.
     * @return {@code true} if a donor with the given name was found and updated, {@code false} otherwise.
     */
    public boolean editDonor(String donorName, Donor updatedDonor) {
        Optional<Donor> donorToEdit = this.donors.stream()
                .filter(donor -> donor.getName().equals(donorName))
                .findFirst();

        if (donorToEdit.isPresent()) {
            Donor donor = donorToEdit.get();
            // Assuming updatedDonor contains the new values for these specific fields
            donor.setDonationAmount(updatedDonor.getDonationAmount());
            donor.setDonationType(updatedDonor.getDonationType());
            donor.setDonationDate(updatedDonor.getDonationDate());
            // Note: Other fields like donorId, name, totalDonations, lastDonationDate are not updated by this method.
            return true;
        }

        return false; // Donor not found
    }

    /**
     * Removes a donor from the service by their name.
     *
     * @param donorName The name of the donor to remove.
     * @return {@code true} if a donor with the given name was found and removed, {@code false} otherwise.
     */
    public boolean removeDonor(String donorName) {
        return this.donors.removeIf(donor -> donor.getName().equals(donorName));
    }

    /**
     * Retrieves a list of all donors currently managed by this service.
     *
     * @return A new {@link ArrayList} containing all {@link Donor} objects.
     *         This is a copy, so modifications to the returned list will not affect the internal storage.
     *         Returns an empty list if no donors are present.
     */
    public List<Donor> getAllDonors() {
        return new ArrayList<>(this.donors); // Return a copy
    }
}
