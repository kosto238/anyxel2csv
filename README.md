# anyxel2csv
Combination of excel libraries to allow converting to CSV almost any excel you meet.
Libraries used:
1) apache POI
2) JXL (net.sourceforge.jexcelapi)
3) FastExcel (edu.npu.fastexcel, modified)

The idea is that no one of these libraries can`t read any excel by itself only, 
but bunch of them can. So we've built a chain where if one them fail's - another one 
trying to do the job)
