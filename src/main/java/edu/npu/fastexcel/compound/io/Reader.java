/* 
 *FastExcel,(c) copyright 2009 yAma<guooscar@gmail.com>.  
 *WEB: http://fastexcel.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
/**
 * 
 */
package edu.npu.fastexcel.compound.io;

/**
 * This interface specify HOW to read byte from file or some other media.<br/>
 * First of all.The Excel file is a Compound Format File.The content of workbook
 * is just a stream of the compound file,This stream was organized in BIFF
 * format. Sometime we just read file and seek the BOF(Beginning OF File)and
 * EOF(Ending Of File)record is OK,but not every file works.A rigorous way is
 * parsing the compound file format and extract workbook stream.
 * <code>CompoundFile</code> and <code>SectorStream</code> do this job.
 * 
 * @author <a href="guooscar@gmail.com">yAma</a> 2008-11-22
 */
public interface Reader {

	/**
	 * Open this reader and ready to read.
	 * 
	 * @throws ReadException
	 *             when some error occur.
	 */
	public void open() throws ReadException;

	/**
	 * Close this reader and release resource.
	 * 
	 * @throws ReadException
	 *             when some error occur.
	 */
	public void close() throws ReadException;

	/**
	 * Get next byte.If there is not any more byte return -1.
	 * 
	 * @return the bytes of next record.
	 * @throws ReadException
	 *             when some error occur.
	 */
	public byte read() throws ReadException;

	/**
	 * Get byte from stream and store it at b.
	 * 
	 * @param b
	 *            result holder.
	 * @return if stream end return -1
	 * @throws ReadException
	 *             when some error occur.
	 */
	public int read(byte b[]) throws ReadException;

	/**
	 * Get byte from stream and store it at b.
	 * 
	 * @param b
	 *            result holder.
	 * @param offset
	 *            which position we holder result.
	 * @param length
	 *            how many bytes to read.
	 * @return if stream end return -1
	 * @throws ReadException
	 *             when some error occur.
	 */
	public int read(byte b[], int offset, int length) throws ReadException;

	/**
	 * @return length of this stream.
	 * @throws ReadException
	 */
	public long length() throws ReadException;

	/**
	 * Get the current position of stream.
	 * 
	 * @return offset from the beginning of this stream.
	 */
	public int position() throws ReadException;

	/**
	 * Seek to position.
	 * 
	 * @param position
	 *            the position
	 */
	public void seek(int pos) throws ReadException;

}