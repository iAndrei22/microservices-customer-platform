export type Customer = {
  id: string;
  name: string;
  email: string;
  address: string;
  dateOfBirth: string;
};

/** Fields collected in the UI and sent on create (registeredDate added in API layer). */
export type CreateCustomerInput = {
  name: string;
  email: string;
  address: string;
  dateOfBirth: string;
};

export type UpdateCustomerPayload = {
  name: string;
  email: string;
  address: string;
  dateOfBirth: string;
};
