package xxl.core.collections.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.Enumerator;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.io.converters.FixedSizeConverter;
import xxl.core.io.converters.LongConverter;

/**
 * Stores entries in {@link ArrayList}. Long values represents index values.
 * Note this main memory container does not support remove operation!
 * Maximal number of objects are limited by positive integer value.
 * 
 */
public class ArrayContainer extends AbstractContainer{
	
	
	private ArrayList<Object> array;
	/**
	 * A unique object used to identify mappings where no object has been
	 * assigned to so far.
	 */
	protected static final Object empty = new Object();
	
	/**
	 * A counter that is used to create unique ids. Everytime an object is
	 * inserted into the container the counter is increased and a
	 * <tt>Long</tt> object with the actual value of the counter is
	 * returned as id.
	 */
	protected int counter = 0;
	/**
	 * 
	 * @param initialSize
	 */
	public ArrayContainer(int initialSize){
		array = new ArrayList<Object>(initialSize); 
	}
	/**
	 * 
	 */
	public ArrayContainer(){
		this(2048);
	}
	/**
	 * Returns a converter for the ids generated by this container. A
	 * converter transforms an object to its byte representation and vice
	 * versa - also known as serialization in Java.<br>
	 * Because this container is returning <tt>Long</tt> objects as ids
	 * the converter <code>LongConverter.DEFAULT_INSTANCE</code> is
	 * returned.
	 *
	 * @return a converter for serializing the identifiers of the
	 *         container.
	 * @see LongConverter#DEFAULT_INSTANCE
	 */
	public FixedSizeConverter objectIdConverter () {
		return LongConverter.DEFAULT_INSTANCE;
	}
	/**
	 * Returns the size of the ids generated by this container in bytes,
	 * which is 8.
	 * @return 8
	 */
	public int getIdSize() {
		return LongConverter.SIZE;
	}
	/**
	 * Removes all elements from this container. After a call of this
	 * method, <tt>size()</tt> will return 0.
	 */
	public void clear () {
		array.clear();
	}
	/**
	 * Returns <tt>true</tt> if there is an object stored within the container
	 * having the identifier <tt>id</tt>.
	 *
	 * @param id identifier of the object.
	 * @return true if the container contains an object for the specified
	 *         identifier.
	 */
	public boolean contains (Object id) {
		int index = ((Long)id).intValue(); 
		return index < array.size();
	}
	/**
	 * Returns the object associated to the identifier <tt>id</tt>. An
	 * exception is thrown if there is not object stored with this
	 * <tt>id</tt>. The parameter <tt>unfix</tt> has no function because this
	 * container is unbuffered.
	 *
	 * @param id identifier of the object.
	 * @param unfix signals whether the object can be removed from the
	 *        underlying buffer.
	 * @return the object associated to the specified identifier.
	 * @throws NoSuchElementException if the desired object is not found.
	 */
	public Object get (Object id, boolean unfix) throws NoSuchElementException {
		if (!contains(id))
			throw new NoSuchElementException();
		else{
			int index = ((Long)id).intValue(); 
			return array.get(index); 
		}
	}

	/**
	 * Returns an iterator that delivers all the identifiers of
	 * the container that are in use.
	 *
	 * @return an iterator of all identifiers used by this container.
	 */
	public Iterator ids () {
		return new Mapper<Integer, Long>(new AbstractFunction<Integer, Long>() {
			@Override
			public Long invoke(Integer argument) {
				return Long.valueOf(argument.longValue());
			}
		}, new Enumerator(array.size())) ; 
	
	}

	/**
	 * Inserts a new object into the container and returns the unique
	 * identifier that the container has been associated to the object.
	 * This container uses a counter to generate an unique id. Everytime
	 * an object is inserted into the container the counter is increased
	 * and a <tt>Long</tt> object with the actual value of the counter is
	 * returned as id. So the identifier will not be reused again when the
	 * object is deleted from the container. The parameter <tt>unfix</tt> 
	 * has no function because this container is unbuffered.
	 *
	 * @param object is the new object.
	 * @param unfix signals a buffered container whether the object can
	 *        be removed from the underlying buffer.
	 * @return the identifier of the object.
	 */
	public Object insert (Object object, boolean unfix) {
		int id = counter++;
		array.add(object);
		return Long.valueOf(id);
	}

	/**
	 * Checks whether the <tt>id</tt> has been returned previously by a
	 * call to insert or reserve and hasn't been removed so far.
	 *
	 * @param id the id to be checked.
	 * @return true exactly if the <tt>id</tt> is still in use.
	 */
	public boolean isUsed (Object id) {
		int index = ((Long)id).intValue(); 
		return index < array.size();
	}

	/**
	 * this method always throws {@link UnsupportedOperationException}, since this main memory container does not support remove operations.
	 * @throws UnsupportedOperationException
	 */
	public void remove (Object id) throws NoSuchElementException {
		throw new UnsupportedOperationException("Array container does not support Remove Operation!");
	}

	/**
	 * Reserves an id for subsequent use. The container may or may not
	 * need an object to be able to reserve an id, depending on the
	 * implementation. If so, it will call the parameterless function
	 * provided by the parameter <tt>getObject</tt>.
	 *
	 * @param getObject A parameterless function providing the object for
	 * 			that an id should be reserved.
	 * @return the reserved id.
	*/
	public Object reserve (Function getObject) {
		int id = counter++;
		array.add(empty);
		return Long.valueOf(id);
	}

	/**
	 * Returns the number of elements of the container.
	 *
	 * @return the number of elements.
	 */
	public int size () {
		return array.size();
	}

	/**
	 * Overwrites an existing (id,*)-element by (id, object). This method
	 * throws an exception if an object with an identifier <tt>id</tt>
	 * does not exist in the container.
	 *
	 * @param id identifier of the element.
	 * @param object the new object that should be associated to
	 *        <tt>id</tt>.
	 * @param unfix signals a buffered container whether the object can
	 *        be removed from the underlying buffer.
	 * @throws NoSuchElementException if an object with an identifier
	 *         <tt>id</tt> does not exist in the container.
	 */
	public void update (Object id, Object object, boolean unfix) throws NoSuchElementException {
		if (isUsed(id)) {
			int index = ((Long)id).intValue(); 
			array.set(index, object); 
		}
		else
			throw new NoSuchElementException();
	}
}