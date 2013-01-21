/**
 * A basic JircBot module. It is fed in sanitized lines, and returns something
 * to say. It can hold a state if it wishes.
 */
interface JircModule {
	String feed(Message message);
}