package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.DonorContact;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link DonorContact} information.
 * This class provides functionalities to add, edit, remove, and retrieve donor data.
 * Donor information is stored in an in-memory list.
 */
public class DonorService {

    /** Shared list storing donors across service instances. */
    private static final List<DonorContact> SHARED_DONORS = new ArrayList<>();

    /** In-memory list to store {@link DonorContact} objects. */
    private final List<DonorContact> donors;

    /**
     * Constructs a new {@code DonorService}.
     * Initializes an empty list to store donors.
     */
    public DonorService() {
        this.donors = SHARED_DONORS;
    }

    /**
     * Adds a new donor to the service.
     *
     * @param donor The {@link DonorContact} object to add. Must not be null.
     * @throws NullPointerException if {@code donor} is null (due to ArrayList behavior).
     */
    public void addDonor(DonorContact donor) {
        this.donors.add(donor);
    }

    /**
     * Edits an existing donor's information identified by name.
     * Only the basic contact fields are currently updated from {@code updatedDonor}.
     *
     * @param donorName The name of the donor to edit.
     * @param updatedDonor A {@link DonorContact} object containing the updated information.
     * @return {@code true} if a donor with the given name was found and updated, {@code false} otherwise.
     */
    public boolean editDonor(String donorName, DonorContact updatedDonor) {
        Optional<DonorContact> donorToEdit = this.donors.stream()
                .filter(donor -> donor.getName().equals(donorName))
                .findFirst();

        if (donorToEdit.isPresent()) {
            DonorContact donor = donorToEdit.get();
            donor.setEmail(updatedDonor.getEmail());
            donor.setPhone(updatedDonor.getPhone());
            donor.setName(updatedDonor.getName());
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
     * @return A new {@link ArrayList} containing all {@link DonorContact} objects.
     *         This is a copy, so modifications to the returned list will not affect the internal storage.
     *         Returns an empty list if no donors are present.
     */
    public List<DonorContact> getAllDonors() {
        return new ArrayList<>(this.donors); // Return a copy
    }

    /**
     * Saves all donors to a JSON file located in the given company directory.
     *
     * @param companyDirectory directory where the donors file should be written
     * @throws IOException if writing fails or the directory is invalid
     */
    public void saveDonors(java.io.File companyDirectory) throws java.io.IOException {
    try {
        nonprofitbookkeeping.persistence.DonorRepository repo = new nonprofitbookkeeping.persistence.DonorRepository();
        for (nonprofitbookkeeping.model.DonorContact d : getAllDonors()) {
            repo.upsert(d);
        }
    } catch (Exception e) {
        throw new java.io.IOException("Failed to save donors to H2 database", e);
    }
}

    /**
     * Loads donors from a JSON file located in the given company directory.
     * Existing in-memory donors are cleared before loading new ones. If the
     * file does not exist, this method simply returns with an empty list.
     *
     * @param companyDirectory directory where the donors file is located
     * @throws IOException if reading fails or the directory is invalid
     */
    public void loadDonors(java.io.File companyDirectory) throws java.io.IOException {
    try {
        nonprofitbookkeeping.persistence.DonorRepository repo = new nonprofitbookkeeping.persistence.DonorRepository();
        this.donors.clear();
        this.donors.addAll(repo.list());
    } catch (Exception e) {
        throw new java.io.IOException("Failed to load donors from H2 database", e);
    }
}
}

