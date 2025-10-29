package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.DonorContact;
import nonprofitbookkeeping.persistence.DonorRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service class for managing {@link DonorContact} information.
 * This class provides functionalities to add, edit, remove, and retrieve donor data.
 * Donor information is stored in an in-memory list.
 */
public class DonorService {

    /** Repository responsible for persisting donors. */
    private final DonorRepository repository;
    /** In-memory list to store {@link DonorContact} objects. */
    private final List<DonorContact> donors;

    /**
     * Constructs a new {@code DonorService}.
     * Initializes an empty list to store donors.
     */
    public DonorService() {
        this(new DonorRepository());
    }

    DonorService(DonorRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.donors = new ArrayList<>();
    }

    /**
     * Adds a new donor to the service.
     *
     * @param donor The {@link DonorContact} object to add. Must not be null.
     * @throws NullPointerException if {@code donor} is null (due to ArrayList behavior).
     */
    public void addDonor(DonorContact donor) {
        if (donor == null) {
            return;
        }

        if (donor.getId() == null || donor.getId().isBlank()) {
            donor.setId(UUID.randomUUID().toString());
        }

        try {
            this.repository.upsert(donor);
            reloadFromRepository();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to persist donor", e);
        }
    }

    /**
     * Edits an existing donor's information identified by name.
     * Only the basic contact fields are currently updated from {@code updatedDonor}.
     *
     * @param donorName The name of the donor to edit.
     * @param updatedDonor A {@link DonorContact} object containing the updated information.
     * @return {@code true} if a donor with the given name was found and updated, {@code false} otherwise.
     */
    public boolean editDonor(String donorId, DonorContact updatedDonor) {
        if (donorId == null || donorId.isBlank() || updatedDonor == null) {
            return false;
        }

        if (updatedDonor.getId() == null || updatedDonor.getId().isBlank()) {
            updatedDonor.setId(donorId);
        }

        try {
            this.repository.upsert(updatedDonor);
            reloadFromRepository();
            return this.donors.stream().anyMatch(d -> donorId.equals(d.getId()));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update donor", e);
        }
    }

    /**
     * Removes a donor from the service by their name.
     *
     * @param donorName The name of the donor to remove.
     * @return {@code true} if a donor with the given name was found and removed, {@code false} otherwise.
     */
    public boolean removeDonor(String donorId) {
        if (donorId == null || donorId.isBlank()) {
            return false;
        }

        try {
            boolean removed = this.repository.deleteByExternalId(donorId);
            reloadFromRepository();
            return removed;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete donor", e);
        }
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
            this.repository.replaceAll(this.donors);
        } catch (SQLException e) {
            throw new IOException("Failed to save donors to H2 database", e);
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
            reloadFromRepository();
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof SQLException sqlException) {
                throw new IOException("Failed to load donors from H2 database", sqlException);
            }
            throw ex;
        }
    }

    private void reloadFromRepository() {
        try {
            this.donors.clear();
            this.donors.addAll(this.repository.list());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read donors from database", e);
        }
    }
}

