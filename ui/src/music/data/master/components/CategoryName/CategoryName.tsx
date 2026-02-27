import { useCategory } from '../../hooks/useCategory.ts';

interface CategoryNameProps {
  categoryId: number;
  fallback?: string;
}

export const CategoryName = ({ categoryId, fallback = `Category ID: ${categoryId}` }: CategoryNameProps) => {
  const { data: category, isLoading } = useCategory(categoryId);

  if (isLoading) return <span>{fallback}</span>;
  
  return <span>{category?.name || fallback}</span>;
};
